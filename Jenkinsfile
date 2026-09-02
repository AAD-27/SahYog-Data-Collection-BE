pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timestamps()
    }

    triggers {
        githubPush()
        // GitHub webhook is immediate; one-minute polling is the localhost fallback.
        pollSCM('* * * * *')
    }

    environment {
        IMAGE_NAME = 'sahyog/dc'
        CONTAINER_NAME = 'sahyog-dc'
        APP_PORT = '8091'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    def shortCommit = sh(script: 'git rev-parse --short=8 HEAD', returnStdout: true).trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${shortCommit}"
                    currentBuild.description = "Commit ${env.GIT_COMMIT ?: shortCommit}"
                }
                sh '''
                    docker run --rm \
                      --user "$(id -u):$(id -g)" \
                      --volumes-from jenkins \
                      --volume sahyog-maven-cache:/root/.m2 \
                      --workdir "${WORKSPACE}" \
                      maven:3.9-eclipse-temurin-17 \
                      mvn --batch-mode --update-snapshots clean compile
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    docker run --rm \
                      --user "$(id -u):$(id -g)" \
                      --volumes-from jenkins \
                      --volume sahyog-maven-cache:/root/.m2 \
                      --workdir "${WORKSPACE}" \
                      maven:3.9-eclipse-temurin-17 \
                      mvn --batch-mode test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Sonar Quality Gate') {
            steps {
                withSonarQubeEnv('SonarQubeScanner') {
                    sh '''
                        test "$(find src/main/java -type f -name '*.java' | wc -l)" -gt 0 || {
                          echo "No main Java source files found; refusing an empty SonarQube analysis."
                          exit 1
                        }

                        docker run --rm \
                          --user "$(id -u):$(id -g)" \
                          --volumes-from jenkins \
                          --volume sahyog-maven-cache:/root/.m2 \
                          --network devops-net \
                          --workdir "${WORKSPACE}" \
                          --env SONAR_HOST_URL="${SONAR_HOST_URL}" \
                          --env SONAR_TOKEN="${SONAR_AUTH_TOKEN}" \
                          maven:3.9-eclipse-temurin-17 \
                          mvn --batch-mode org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                            -Dsonar.host.url="${SONAR_HOST_URL}" \
                            -Dsonar.token="${SONAR_AUTH_TOKEN}" \
                            -Dsonar.projectKey=sahyog-dc \
                            -Dsonar.projectName="SahYog DC" \
                            -Dsonar.maven.scanAll=false \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.inclusions="src/main/java/**/*.java" \
                            -Dsonar.test.inclusions="src/test/java/**/*.java" \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.test.binaries=target/test-classes \
                            -Dsonar.qualitygate.wait=true \
                            -Dsonar.qualitygate.timeout=300
                    '''
                }
            }
        }

        stage('Package') {
            steps {
                sh '''
                    docker run --rm \
                      --user "$(id -u):$(id -g)" \
                      --volumes-from jenkins \
                      --volume sahyog-maven-cache:/root/.m2 \
                      --workdir "${WORKSPACE}" \
                      maven:3.9-eclipse-temurin-17 \
                      mvn --batch-mode package -DskipTests
                '''
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build image') {
            when { expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' } }
            steps {
                sh 'docker build --pull --tag ${IMAGE_NAME}:${BUILD_NUMBER} --tag ${IMAGE_NAME}:latest .'
            }
        }

        stage('Deploy') {
            when { expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' } }
            steps {
                withCredentials([
                    string(credentialsId: 'sahyog-mysql-password', variable: 'MYSQL_PASSWORD'),
                    string(credentialsId: 'sahyog-auth-jwt-secret', variable: 'AUTH_JWT_SECRET'),
                    string(credentialsId: 'sahyog-auth-aadhaar-pepper', variable: 'AUTH_AADHAAR_PEPPER')
                ]) {
                    sh '''
                        docker rm --force "${CONTAINER_NAME}" 2>/dev/null || true
                        for attempt in 1 2 3 4 5 6; do
                          if docker run --detach \
                            --name "${CONTAINER_NAME}" \
                            --restart unless-stopped \
                            --add-host host.docker.internal:host-gateway \
                            --publish "${APP_PORT}:${APP_PORT}" \
                            --env MYSQL_PASSWORD="${MYSQL_PASSWORD}" \
                            --env AUTH_JWT_SECRET="${AUTH_JWT_SECRET}" \
                            --env AUTH_AADHAAR_PEPPER="${AUTH_AADHAAR_PEPPER}" \
                            --env SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/SAHYOG_DB?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true" \
                            "${IMAGE_NAME}:${BUILD_NUMBER}"; then
                            exit 0
                          fi
                          docker rm --force "${CONTAINER_NAME}" 2>/dev/null || true
                          echo "Port ${APP_PORT} is not released yet; retrying deployment (${attempt}/6)..."
                          sleep 5
                        done
                        exit 1
                    '''
                }
            }
        }

        stage('Verify deployment') {
            when { expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' } }
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    sh '''
                        until [ "$(docker inspect --format='{{.State.Health.Status}}' "${CONTAINER_NAME}" 2>/dev/null)" = healthy ]; do
                          [ "$(docker inspect --format='{{.State.Status}}' "${CONTAINER_NAME}" 2>/dev/null)" = exited ] && docker logs "${CONTAINER_NAME}" && exit 1
                          sleep 3
                        done
                    '''
                }
            }
        }
    }

    post {
        cleanup { deleteDir() }
    }
}
