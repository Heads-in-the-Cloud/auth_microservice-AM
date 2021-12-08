pipeline {
    agent { label 'aws-ready' }

    environment {
        commit = sh(returnStdout: true, script: "git rev-parse --short=8 HEAD").trim()
        aws_region = 'us-west-2'
        aws_ecr_repo = '026390315914'
        repo_name = 'am-auth-api'
    }

    stages {
        stage('System information') {
            steps {
                echo 'Debug info:'
                sh 'ls'
                sh 'pwd'
            }
        }
        stage('AWS') {
            steps {
                echo 'logging in via AWS client'
                sh 'aws ecr get-login-password --region ${aws_region} | docker login --username AWS --password-stdin ${aws_ecr_repo}.dkr.ecr.${aws_region}.amazonaws.com'
            }
        }
        stage('Build') {
            steps {
                echo 'Building Docker image'
                sh 'docker build -t ${repo_name} .'
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
