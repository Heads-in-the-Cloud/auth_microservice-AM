pipeline {
    agent { label 'aws-ready' }

    environment {
        commit = sh(returnStdout: true, script: "git rev-parse --short=8 HEAD").trim()
        aws_region = "${sh(script:'aws configure get region', returnStdout: true).trim()}"
        aws_ecr_repo = "${sh(script:'aws sts get-caller-identity --query "Account" --output text', returnStdout: true).trim()}"
        repo_name = 'am-auth-api'
        jar_name = 'auth-0.0.1-SNAPSHOT.jar'
        sonarRunner = tool name: 'SonarQubeScanner'
    }

    stages {
        stage('AWS') {
            steps {
                echo 'logging in via AWS client'
                sh 'aws ecr get-login-password --region ${aws_region} | docker login --username AWS --password-stdin ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com'
            }
        }
        stage('Package') {
            steps {
                echo 'Building Docker image'
                sh 'docker context use default'
                sh 'mvn -f pom.xml clean package'
            }
        }
        stage('SonarQube') {
            steps {
                echo 'Running SonarQube Quality Analysis'
                withSonarQubeEnv('SonarQube') {
                    sh """
                       ${sonarRunner}/bin/sonar-scanner \
                       -Dsonar.projectKey=AM-auth-api \
                       -Dsonar.sources=./src/main/java/com/ss/training/utopia/auth \
                       -Dsonar.java.binaries=./target/classes/com/ss/training/utopia/auth
                    """
                }
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Build') {
            steps {
                echo 'Building Docker Image'
                sh 'docker build --build-arg jar_name=${jar_name} -t ${repo_name} .'
            }
        }
        stage('Push Images') {
            steps {
                echo 'Tagging images'
                sh 'docker tag ${repo_name}:latest ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com/${repo_name}:latest'
                sh 'docker tag ${repo_name}:latest ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com/${repo_name}:${commit}'
                echo 'Pushing images'
                sh 'docker push ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com/${repo_name}:latest'
                sh 'docker push ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com/${repo_name}:${commit}'
            }
        }
        stage('Cleanup') {
            steps {
                echo 'Removing images'
                sh 'docker rmi ${repo_name}:latest'
                sh 'docker rmi ${aws_ecr_repo}.dkr.ecr.us-west-2.amazonaws.com/${repo_name}:latest'
                sh 'docker rmi ${aws_ecr_repo}.dkr.ecr.us-west-2.amazonaws.com/${repo_name}:${commit}'
            }
        }
    }
}
