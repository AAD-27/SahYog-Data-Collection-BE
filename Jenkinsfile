pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timestamps()
    }

    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    environment {
        IMAGE_NAME = 'sahyog/dc'
        CONTAINER_NAME = 'sahyog-dc'
        APP_PORT = '8091'
    }

    stages {
        stage('Build and test') {
            steps {
                sh '''
                    docker run --rm \
                      --user "$(id -u):$(id -g)" \
                      --volumes-from jenkins \
                      --workdir "${WORKSPACE}" \
                      maven:3.9-eclipse-temurin-17 \
                      mvn --batch-mode --update-snapshots clean verify
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
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
                withCredentials([string(credentialsId: 'sahyog-mysql-password', variable: 'MYSQL_PASSWORD')]) {
                    sh '''
                        docker rm --force "${CONTAINER_NAME}" 2>/dev/null || true
                        docker run --detach \
                          --name "${CONTAINER_NAME}" \
                          --restart unless-stopped \
                          --add-host host.docker.internal:host-gateway \
                          --publish "${APP_PORT}:${APP_PORT}" \
                          --env MYSQL_PASSWORD="${MYSQL_PASSWORD}" \
                          --env SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/SAHYOG_DB?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true" \
                          "${IMAGE_NAME}:${BUILD_NUMBER}"
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
