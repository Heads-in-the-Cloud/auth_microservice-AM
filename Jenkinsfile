pipeline {
    agent { label 'aws-ready' }

    environment {
        // General
        COMMIT_HASH     = sh(returnStdout: true, script: "git rev-parse --short=8 HEAD").trim()
        API_REPO_NAME   = 'am-auth-api'
        JARFILE_NAME    = 'auth-0.0.1-SNAPSHOT.jar'
        SONARQUBE_ID    = tool name: 'SonarQubeScanner-4.6.2'

        // AWS Specific
        AWS_PROFILE     = "${AWS_PROFILE_NAME}"
        SECRET_ID_PUSH  = "${AM_SECRET_ID_PUSH}"
    }

    stages {

        stage('AWS Setup') {
            steps {
                echo 'logging in via AWS client'
                sh 'aws ecr get-login-password --region ${AWS_REGION_ID} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com'
            }
        }

        stage('Package Project') {
            steps {
                echo 'Cleaning Maven package'
                sh 'docker context use default'
                sh 'mvn -f pom.xml clean package'
            }
        }

        stage('SonarQube Scan') {
            steps {
                echo 'Running SonarQube Quality Analysis'
                withSonarQubeEnv('SonarQube') {
                    sh """
                       ${SONARQUBE_ID}/bin/sonar-scanner \
                       -Dsonar.projectKey=AM-auth-api \
                       -Dsonar.sources=./src/main/java/com/ss/training/utopia/auth \
                       -Dsonar.java.binaries=./target/classes/com/ss/training/utopia/auth
                    """
                }
                timeout(time: 5, unit: 'MINUTES') {
                    sleep(10)
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Image') {
            steps {
                echo 'Building Docker Image'
                sh 'docker build --build-arg jar_name=${JARFILE_NAME} -t ${API_REPO_NAME} .'
            }
        }

        stage('Push Images') {
            steps {
                echo 'Tagging images'
                sh 'docker tag ${API_REPO_NAME}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:latest'
                sh 'docker tag ${API_REPO_NAME}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:${COMMIT_HASH}'
                echo 'Pushing images'
                sh 'docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:latest'
                sh 'docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:${COMMIT_HASH}'
            }
        }

        stage('Clean up Images') {
            steps {
                echo 'Removing images'
                sh 'docker rmi ${API_REPO_NAME}:latest'
                sh 'docker rmi ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:latest'
                sh 'docker rmi ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}:${COMMIT_HASH}'
            }
        }

        stage('EKS Update') {
            steps {
                echo 'Configuring Profile and Region'
                sh 'aws configure set region ${AWS_REGION_ID} --profile ${AWS_PROFILE_NAME}'

                echo 'Writing output to Secrets'
                script {
                    // get secret
                    secret = sh(returnStdout: true, script: 'aws secretsmanager get-secret-value --secret-id ${AM_SECRET_ID} | jq -Mr \'.SecretString\'').trim()
                    def jsonObj = readJSON text: secret

                    // update secret
                    jsonObj.AUTH_API_LATEST = env.COMMIT_HASH

                    // push secret
                    String jsonOut = writeJSON returnText: true, json: jsonObj
                    sh "aws secretsmanager update-secret --secret-id 'arn:aws:secretsmanager:${AWS_REGION_ID}:${AWS_ACCOUNT_ID}:secret:${SECRET_ID_PUSH}' --region ${AWS_REGION_ID} --secret-string '${jsonOut}'"
                }
            }
        }

        // end stages
    }
}
