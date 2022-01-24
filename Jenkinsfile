pipeline {
    agent { label 'aws-ready' }

    environment {
        COMMIT_HASH = sh(returnStdout: true, script: "git rev-parse --short=8 HEAD").trim()
        AWS_REGION_ID = "${sh(script:'aws configure get region', returnStdout: true).trim()}"
        AWS_ACCOUNT_ID = "${sh(script:'aws sts get-caller-identity --query "Account" --output text', returnStdout: true).trim()}"
        API_REPO_NAME = 'am-auth-api'
        JARFILE_NAME = 'auth-0.0.1-SNAPSHOT.jar'
        SONARQUBE_ID = tool name: 'SonarQubeScanner-4.6.2'
    }

    stages {
        stage('AWS') {
            steps {
                echo 'logging in via AWS client'
                sh 'aws ecr get-login-password --region ${AWS_REGION_ID} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com'
            }
        }
        stage('Package') {
            steps {
                echo 'Cleaning Maven package'
                sh 'docker context use default'
                sh 'mvn -f pom.xml clean package'
            }
        }
        stage('SonarQube') {
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
                timeout(time: 15, unit: 'MINUTES') {
                    sleep(10)
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Build') {
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
        stage('Cleanup') {
            steps {
                echo 'Removing images'
                sh 'docker rmi ${API_REPO_NAME}:latest'
                sh 'docker rmi ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/${API_REPO_NAME}:latest'
                sh 'docker rmi ${AWS_ACCOUNT_ID}.dkr.ecr.us-west-2.amazonaws.com/${API_REPO_NAME}:${COMMIT_HASH}'
            }
        }
        stage('ECS Update') {
            steps {
                echo 'Attempting to update ECS Deployment data'
                dir("${AM_RESOURCES_DIRECTORY}") {
                    sh 'jq -M --arg commit "${COMMIT_HASH}" \'.auth=$commit\' images.json > tmp.$$.json && mv tmp.$$.json images.json'
                }
            }
        }
    }
}
