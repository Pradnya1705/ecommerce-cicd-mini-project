
pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        IMAGE_NAME = 'pradnya2005/ecommerce-store'
        K8S_NAMESPACE = 'default'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=ecommerce-store'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
 
stage('Build Docker Image') {
    steps {
        bat '''
            "C:\\Users\\Pradnya\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" version
            if errorlevel 1 exit /b 1
            "C:\\Users\\Pradnya\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" build -t "%IMAGE_NAME%:%BUILD_NUMBER%" .
            if errorlevel 1 exit /b 1
        '''
    }
}


       

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    bat '''
                        @echo off
                        echo Logging in to Docker Hub...
                        echo %DOCKER_PASS% | docker login -u "%DOCKER_USER%" --password-stdin
                        if errorlevel 1 exit /b 1

                        docker push "%IMAGE_NAME%:%BUILD_NUMBER%"
                        if errorlevel 1 exit /b 1

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')
                ]) {
                    bat '''
                        @echo off
                        set "KUBECONFIG=%KUBECONFIG_FILE%"

                        kubectl config current-context
                        if errorlevel 1 exit /b 1

                        kubectl set image deployment/ecommerce ecommerce=%IMAGE_NAME%:%BUILD_NUMBER% -n %K8S_NAMESPACE%
                        if errorlevel 1 exit /b 1

                        kubectl rollout status deployment/ecommerce -n %K8S_NAMESPACE% --timeout=120s
                        if errorlevel 1 exit /b 1
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline passed: quality gate approved and deployment completed.'
        }
        failure {
            echo 'Pipeline failed. Check Console Output for the first failed stage.'
        }
    }
}
