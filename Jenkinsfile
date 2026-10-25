pipeline {
  agent any
  tools { jdk 'jdk17'; maven 'maven3' }
  environment {
    SONAR_HOST_URL = 'http://sonarqube:9000'
    IMAGE_NAME = 'pradnya2005/ecommerce-store'
    K8S_NAMESPACE = 'default'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build and Test') {
      steps { bat 'mvn -B clean verify' }
      post { always { junit 'target/surefire-reports/*.xml' } }
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
      steps { sh 'docker build -t $IMAGE_NAME:$BUILD_NUMBER .' }
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
                docker login -u "%DOCKER_USER%" -p "%DOCKER_PASS%"
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
        withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
          sh '''
            export KUBECONFIG="$KUBECONFIG_FILE"
            sed "s|IMAGE_PLACEHOLDER|$IMAGE_NAME:$BUILD_NUMBER|g" k8s/deployment.yaml | kubectl apply -f -
            kubectl apply -f k8s/service.yaml
            kubectl rollout status deployment/ecommerce --timeout=120s
          '''
        }
      }
    }
  }
  post {
    success { echo 'Pipeline passed: quality gate approved and deployment completed.' }
    failure { echo 'Pipeline failed. Review the failed stage; deployment does not proceed after a failed quality gate.' }
  }
}
