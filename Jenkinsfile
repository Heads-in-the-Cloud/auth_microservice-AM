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
        DEPLOY_MODE     = "${AM_DEPLOY_ENVIRONMENT}"

        // Secrets Manager
        SECRET_PUSHES   = credentials("AM_SECRETS_PUSH_JSON")
        SECRET_PULL     = sh(returnStdout: true, script: "echo '${SECRET_PUSHES}' | jq '.${DEPLOY_MODE.toUpperCase()}' | tr -d '\\n\"'")
        SECRET_BASE     = credentials("AM_SECRET_ID_BASE")
        SECRET_ID       = "${DEPLOY_MODE}/${SECRET_BASE}"
        SECRET_ID_PUSH  = "${SECRET_ID}-${SECRET_PULL}"

        // Artifact Information
        CUR_REPO_TYPE   = "${AM_CURRENT_REPO_TYPE}"
        ART_REPO_NAME   = credentials("AM_ARTIFACTORY_ENDPOINT")
        ART_REPO_LOGIN  = credentials("AM_ARTIFACTORY_LOGIN")

        // Repositories
        ECR_REPO_LOC    = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com/${API_REPO_NAME}"
        ART_REPO_LOC    = "${ART_REPO_NAME}/am-utopia/${API_REPO_NAME}"
        CUR_REPO_LOC    = getRepoLoc(CUR_REPO_TYPE, ECR_REPO_LOC, ART_REPO_LOC)
    }

    stages {
        stage('ECR Login') {
            when { expression { CUR_REPO_TYPE == 'ECR' } }
            steps {
                echo 'logging in via AWS client'
                sh 'aws ecr get-login-password --region ${AWS_REGION_ID} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION_ID}.amazonaws.com'
            }
        }

        stage('Artifactory Login') {
            when { expression { CUR_REPO_TYPE == 'ART' } }
            steps {
                echo 'logging in via docker login'
                sh 'echo ${ART_REPO_LOGIN_PSW} | docker login ${ART_REPO_NAME} --username ${ART_REPO_LOGIN_USR} --password-stdin'
            }
        }

        stage('Package Project') {
            steps {
                echo 'Cleaning Maven package'
                sh 'docker context use default'
                sh 'mvn -f pom.xml clean package'
            }
        }

        stage('SonarQube Quality Gate') {
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

        stage('Build Project') {
            steps {
                echo 'Building Docker Image'
                sh 'docker build --build-arg jar_name=${JARFILE_NAME} -t ${API_REPO_NAME} .'
            }
        }

        stage('Push Images') {
            steps {
                echo 'Tagging images'
                sh 'docker tag ${API_REPO_NAME}:latest ${CUR_REPO_LOC}:latest'
                sh 'docker tag ${API_REPO_NAME}:latest ${CUR_REPO_LOC}:${COMMIT_HASH}'
                echo 'Pushing images'
                sh 'docker push ${CUR_REPO_LOC}:latest'
                sh 'docker push ${CUR_REPO_LOC}:${COMMIT_HASH}'
            }
        }

        stage('Clean Images') {
            steps {
                echo 'Removing images'
                sh 'docker rmi ${API_REPO_NAME}:latest'
                sh 'docker rmi ${CUR_REPO_LOC}:latest'
                sh 'docker rmi ${CUR_REPO_LOC}:${COMMIT_HASH}'
            }
        }

        stage('Update Secrets') {
            steps {
                echo 'Configuring Profile and Region'
                sh 'aws configure set region ${AWS_REGION_ID} --profile ${AWS_PROFILE_NAME}'

                echo 'Writing output to Secrets'
                script {
                    // get secret
                    secret = sh(returnStdout: true, script: 'aws secretsmanager get-secret-value --secret-id ${SECRET_ID} | jq -Mr \'.SecretString\'').trim()
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

// Decide which repo to use based on current type; default to ECR
def getRepoLoc(repoType, ecrLoc, artLoc) {
    if(repoType == "ART") {
        return artLoc
    } else {
        return ecrLoc
    }
}
