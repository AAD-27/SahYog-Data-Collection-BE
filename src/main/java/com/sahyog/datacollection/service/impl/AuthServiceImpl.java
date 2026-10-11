package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.auth.*;
import com.sahyog.datacollection.exception.AuthException;
import com.sahyog.datacollection.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Map<String, String> DC_PAGE_NAMES = Map.ofEntries(
            Map.entry("DC001", "Initiate Data Collection"),
            Map.entry("DC002", "Applicant Details"),
            Map.entry("DC003", "Address Details"),
            Map.entry("DC004", "Person Summary"),
            Map.entry("DC005", "Person Details"),
            Map.entry("DC006", "Program Details"),
            Map.entry("DC007", "Income Summary"),
            Map.entry("DC008", "Income Details"),
            Map.entry("DC009", "Expense Summary"),
            Map.entry("DC010", "Expense Details"),
            Map.entry("DC011", "Resource Summary"),
            Map.entry("DC012", "Resource Details"),
            Map.entry("DC013", "Disability Summary"),
            Map.entry("DC014", "Disability Details"));
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final byte[] jwtSecret;
    private final byte[] aadhaarPepper;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(JdbcTemplate jdbc, PasswordEncoder passwordEncoder,
                           @Value("${auth.jwt.secret}") String jwtSecret,
                           @Value("${auth.aadhaar.pepper}") String aadhaarPepper,
                           @Value("${auth.access-token-seconds}") long accessSeconds,
                           @Value("${auth.refresh-token-seconds}") long refreshSeconds) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecret = requireSecret(jwtSecret, "AUTH_JWT_SECRET");
        this.aadhaarPepper = requireSecret(aadhaarPepper, "AUTH_AADHAAR_PEPPER");
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
    }

    @Override public LoginResponse loginOfficial(LoginRequest request) {
        String id = request.loginId().trim();
        if (id.contains("@") && !id.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw validation("Official email address is invalid");
        return login("GOVERNMENT_OFFICIAL", id, request.password(), null);
    }

    @Override public LoginResponse loginCitizen(LoginRequest request) {
        String id = request.loginId().trim();
        if (!id.matches("(?:\\d{10}|\\d{12})"))
            throw validation("Citizen loginId must be a 10-digit mobile number or 12-digit Aadhaar number");
        String aadhaarHash = id.length() == 12 ? hmac(id, aadhaarPepper) : null;
        return login("CITIZEN", id, request.password(), aadhaarHash);
    }

    private LoginResponse login(String type, String loginId, String password, String aadhaarHash) {
        List<Account> accounts = jdbc.query("""
                SELECT USER_ID, PROFILE_TYPE, EMPLOYEE_ID, CITIZEN_ID, EMAIL, FIRST_NAME,
                       MIDDLE_NAME, LAST_NAME, MOBILE_NUMBER, PASSWORD_HASH, ROLE, ACCOUNT_STATUS
                  FROM DC_USER_ACCOUNT
                 WHERE PROFILE_TYPE=? AND (LOWER(EMPLOYEE_ID)=LOWER(?) OR LOWER(EMAIL)=LOWER(?)
                    OR MOBILE_NUMBER=? OR AADHAAR_LOOKUP_HASH=?)
                """, mapper(), type, loginId, loginId, loginId, aadhaarHash);
        if (accounts.isEmpty() || !passwordEncoder.matches(password, accounts.get(0).passwordHash()))
            throw new AuthException(401, "INVALID_CREDENTIALS", "Invalid login ID or password");
        Account account = accounts.get(0);
        if (!"ACTIVE".equalsIgnoreCase(account.status()))
            throw new AuthException(403, "ACCOUNT_DISABLED", "User account cannot access the system");

        Instant now = Instant.now();
        String accessToken = jwt(account, now, now.plusSeconds(accessSeconds));
        String refreshToken = randomToken();
        jdbc.update("INSERT INTO DC_AUTH_SESSION (USER_ID, REFRESH_TOKEN_HASH, EXPIRES_AT) VALUES (?,?,?)",
                account.userId(), sha256(refreshToken), java.sql.Timestamp.from(now.plusSeconds(refreshSeconds)));
        jdbc.update("UPDATE DC_USER_ACCOUNT SET LAST_LOGIN_AT=? WHERE USER_ID=?",
                java.sql.Timestamp.from(now), account.userId());
        return new LoginResponse(accessToken, refreshToken, "Bearer", accessSeconds, toResponse(account));
    }

    @Override @Transactional
    public CitizenRegistrationResponse registerCitizen(CitizenRegistrationRequest request) {
        if (!request.password().equals(request.confirmPassword()))
            throw validation("password and confirmPassword must match");
        String email = blankToNull(request.email());
        KeyHolder key = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO DC_USER_ACCOUNT
                    (PROFILE_TYPE,FIRST_NAME,MIDDLE_NAME,LAST_NAME,MOBILE_NUMBER,EMAIL,AADHAAR_LOOKUP_HASH,
                     AADHAAR_LAST_FOUR,PASSWORD_HASH,ROLE,ACCOUNT_STATUS,VERIFICATION_REQUIRED)
                    VALUES ('CITIZEN',?,?,?,?,?,?,?,?,'CITIZEN','ACTIVE',FALSE)
                    """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, request.firstName().trim());
                ps.setString(2, blankToNull(request.middleName()));
                ps.setString(3, request.lastName().trim());
                ps.setString(4, request.mobileNumber());
                ps.setString(5, email == null ? null : email.toLowerCase(Locale.ROOT));
                ps.setString(6, hmac(request.aadharNumber(), aadhaarPepper));
                ps.setString(7, request.aadharNumber().substring(8));
                ps.setString(8, passwordEncoder.encode(request.password()));
                return ps;
            }, key);
        } catch (DuplicateKeyException ex) {
            throw new AuthException(409, "ACCOUNT_ALREADY_EXISTS", "Citizen mobile, email, or Aadhaar is already registered");
        }
        long userId = key.getKey().longValue();
        String citizenId = "CT" + String.format("%06d", userId);
        jdbc.update("UPDATE DC_USER_ACCOUNT SET CITIZEN_ID=? WHERE USER_ID=?", citizenId, userId);
        return new CitizenRegistrationResponse(userId, citizenId, "CITIZEN",
                "Account created successfully", false);
    }

    @Override @Transactional
    public OfficialRegistrationResponse registerOfficial(OfficialRegistrationRequest request) {
        if (!request.password().equals(request.confirmPassword()))
            throw validation("password and confirmPassword must match");

        KeyHolder key = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO DC_USER_ACCOUNT
                    (PROFILE_TYPE,EMPLOYEE_ID,FIRST_NAME,MIDDLE_NAME,LAST_NAME,EMAIL,DEPARTMENT,
                     DESIGNATION,PASSWORD_HASH,ROLE,ACCOUNT_STATUS,APPROVAL_STATUS,VERIFICATION_REQUIRED)
                    VALUES ('GOVERNMENT_OFFICIAL',?,?,?,?,?,?,?,?,'CASE_WORKER','ACTIVE','APPROVED',FALSE)
                    """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, request.employeeId().trim().toUpperCase(Locale.ROOT));
                ps.setString(2, request.firstName().trim());
                ps.setString(3, blankToNull(request.middleName()));
                ps.setString(4, request.lastName().trim());
                ps.setString(5, request.officialEmail().trim().toLowerCase(Locale.ROOT));
                ps.setString(6, request.department().trim());
                ps.setString(7, request.designation().trim());
                ps.setString(8, passwordEncoder.encode(request.password()));
                return ps;
            }, key);
        } catch (DuplicateKeyException ex) {
            throw new AuthException(409, "ACCOUNT_ALREADY_EXISTS",
                    "Government Official employee ID or email is already registered");
        }

        long userId = key.getKey().longValue();
        return new OfficialRegistrationResponse(userId,
                request.employeeId().trim().toUpperCase(Locale.ROOT),
                "GOVERNMENT_OFFICIAL", "CASE_WORKER",
                "Government Official account created successfully", "APPROVED");
    }

    @Override @Transactional
    public CitizenApplicationContextResponse getCitizenApplicationContext(CitizenApplicationContextRequest request) {
        String mobile = request == null ? null : blankToNull(request.mobileNumber());
        String aadhaar = request == null ? null : blankToNull(request.aadharNumber());
        if (mobile == null && aadhaar == null)
            throw validation("Either aadharNumber or mobileNumber is required");

        String aadhaarHash = aadhaar == null ? null : hmac(aadhaar, aadhaarPepper);
        String sql;
        Object[] arguments;
        if (mobile != null && aadhaarHash != null) {
            sql = "SELECT USER_ID,CITIZEN_ID,FIRST_NAME,MIDDLE_NAME,LAST_NAME,MOBILE_NUMBER,AADHAAR_LAST_FOUR " +
                    "FROM DC_USER_ACCOUNT WHERE PROFILE_TYPE='CITIZEN' AND ACCOUNT_STATUS='ACTIVE' " +
                    "AND MOBILE_NUMBER=? AND AADHAAR_LOOKUP_HASH=?";
            arguments = new Object[]{mobile, aadhaarHash};
        } else if (mobile != null) {
            sql = "SELECT USER_ID,CITIZEN_ID,FIRST_NAME,MIDDLE_NAME,LAST_NAME,MOBILE_NUMBER,AADHAAR_LAST_FOUR " +
                    "FROM DC_USER_ACCOUNT WHERE PROFILE_TYPE='CITIZEN' AND ACCOUNT_STATUS='ACTIVE' AND MOBILE_NUMBER=?";
            arguments = new Object[]{mobile};
        } else {
            sql = "SELECT USER_ID,CITIZEN_ID,FIRST_NAME,MIDDLE_NAME,LAST_NAME,MOBILE_NUMBER,AADHAAR_LAST_FOUR " +
                    "FROM DC_USER_ACCOUNT WHERE PROFILE_TYPE='CITIZEN' AND ACCOUNT_STATUS='ACTIVE' AND AADHAAR_LOOKUP_HASH=?";
            arguments = new Object[]{aadhaarHash};
        }

        List<CitizenContext> citizens = jdbc.query(sql, (rs, row) -> new CitizenContext(
                rs.getLong("USER_ID"), rs.getString("CITIZEN_ID"), rs.getString("FIRST_NAME"),
                rs.getString("MIDDLE_NAME"), rs.getString("LAST_NAME"), rs.getString("MOBILE_NUMBER"),
                rs.getString("AADHAAR_LAST_FOUR")), arguments);
        if (citizens.isEmpty()) return new CitizenApplicationContextResponse(
                null, null, null, null, null, null, null, null, null, null, null, false);

        CitizenContext citizen = citizens.get(0);
        List<ApplicationContext> applications = jdbc.query("""
                SELECT APPLICATION_NO, APPLICATION_STATUS, PAGE_ID
                  FROM DC_APPLICATION
                 WHERE MOBILE_NUMBER=?
                 ORDER BY UPDATED_DATE DESC, APPLICATION_NO DESC
                 LIMIT 1
                """, (rs, row) -> new ApplicationContext(rs.getString("APPLICATION_NO"),
                rs.getString("APPLICATION_STATUS"), rs.getString("PAGE_ID")), citizen.mobile());

        String fullName = String.join(" ", java.util.stream.Stream.of(
                citizen.firstName(), citizen.middleName(), citizen.lastName())
                .filter(value -> value != null && !value.isBlank()).toList());
        String maskedAadhaar = citizen.aadhaarLastFour() == null ? null : "XXXXXXXX" + citizen.aadhaarLastFour();
        String maskedMobile = citizen.mobile() == null ? null : "XXXXXX" + citizen.mobile().substring(6);
        if (applications.isEmpty()) return new CitizenApplicationContextResponse(citizen.citizenId(), fullName,
                maskedAadhaar, maskedMobile, null, null, null, null, null, null, null, false);

        ApplicationContext application = applications.get(0);
        String caseNumber = application.number();
        String applicationNumber = "A" + caseNumber.substring(1);
        String pageId = application.pageId() == null || application.pageId().isBlank() ? "DC001" : application.pageId();
        String pageName = DC_PAGE_NAMES.getOrDefault(pageId, "Data Collection");
        String status = "COMPLETED".equalsIgnoreCase(application.status())
                || "COMPLETE".equalsIgnoreCase(application.status()) ? "Completed" : "In Progress";
        jdbc.update("""
                UPDATE DC_USER_ACCOUNT
                   SET CURRENT_MODULE='DC', CURRENT_MODULE_NAME='Data Collection',
                       CURRENT_PAGE_ID=?, CURRENT_PAGE_NAME=?
                 WHERE USER_ID=?
                """, pageId, pageName, citizen.userId());
        return new CitizenApplicationContextResponse(citizen.citizenId(), fullName, maskedAadhaar,
                maskedMobile, applicationNumber, caseNumber, status, "DC", "Data Collection",
                pageId, pageName, true);
    }

    @Override public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String type = request.profileType().trim().toUpperCase(Locale.ROOT);
        if (!type.equals("CITIZEN") && !type.equals("GOVERNMENT_OFFICIAL"))
            throw validation("profileType must be CITIZEN or GOVERNMENT_OFFICIAL");
        return new ForgotPasswordResponse("Password reset instructions have been sent", true);
    }

    private String jwt(Account account, Instant issuedAt, Instant expiresAt) {
        String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64("{\"sub\":\"" + account.userId() + "\",\"profileType\":\"" +
                account.profileType() + "\",\"role\":\"" + account.role() + "\",\"iat\":" +
                issuedAt.getEpochSecond() + ",\"exp\":" + expiresAt.getEpochSecond() + "}");
        String unsigned = header + "." + payload;
        return unsigned + "." + hmac(unsigned, jwtSecret);
    }

    private String randomToken() { byte[] bytes = new byte[32]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String sha256(String value) { try { return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch(Exception ex) { throw new IllegalStateException(ex); } }
    private String hmac(String value, byte[] key) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key,"HmacSHA256")); return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8))); } catch(Exception ex) { throw new IllegalStateException(ex); } }
    private String base64(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private static byte[] requireSecret(String value, String environmentName) { if(value == null || value.length() < 32) throw new IllegalStateException(environmentName + " must contain at least 32 characters"); return value.getBytes(StandardCharsets.UTF_8); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private AuthException validation(String message) { return new AuthException(400, "VALIDATION_ERROR", message); }
    private AuthUserResponse toResponse(Account a) {
        String fullName = String.join(" ", java.util.stream.Stream.of(a.firstName(), a.middleName(), a.lastName()).filter(v -> v != null && !v.isBlank()).toList());
        return new AuthUserResponse(a.userId(), a.profileType(), a.employeeId(), a.citizenId(), a.email(), fullName, a.role(), a.mobile());
    }
    private RowMapper<Account> mapper() { return (rs, row) -> new Account(rs.getLong("USER_ID"),rs.getString("PROFILE_TYPE"),rs.getString("EMPLOYEE_ID"),rs.getString("CITIZEN_ID"),rs.getString("EMAIL"),rs.getString("FIRST_NAME"),rs.getString("MIDDLE_NAME"),rs.getString("LAST_NAME"),rs.getString("MOBILE_NUMBER"),rs.getString("PASSWORD_HASH"),rs.getString("ROLE"),rs.getString("ACCOUNT_STATUS")); }
    private record Account(long userId,String profileType,String employeeId,String citizenId,String email,String firstName,String middleName,String lastName,String mobile,String passwordHash,String role,String status) { }
    private record CitizenContext(long userId, String citizenId, String firstName, String middleName,
                                  String lastName, String mobile, String aadhaarLastFour) { }
    private record ApplicationContext(String number, String status, String pageId) { }
}
