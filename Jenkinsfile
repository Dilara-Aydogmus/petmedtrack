pipeline {
    agent any

    environment {
        IMAGE_REPOSITORY = 'dilaraydgms/petmedtrack'
        GCP_PROJECT = 'petmedtrack-510515'
        GKE_CLUSTER = 'petmedtrack-cluster'
        GKE_ZONE = 'us-central1-a'
        K8S_NAMESPACE = 'petmedtrack'
    }

    stages {
        stage('Build and test') {
            steps {
                powershell 'mvn -B clean package'
            }
        }

        stage('Build and push Docker image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    powershell '''
                        $ErrorActionPreference = 'Stop'

                        $tag = "$env:IMAGE_REPOSITORY`:$env:BUILD_NUMBER"
                        $latest = "$env:IMAGE_REPOSITORY`:latest"

                        $env:DOCKERHUB_TOKEN |
                            docker login --username $env:DOCKERHUB_USERNAME --password-stdin

                        docker build -t $tag -t $latest .
                        docker push $tag
                        docker push $latest
                        docker logout
                    '''
                }
            }
        }

stage('Deploy to GKE') {
    steps {
        powershell '''
            $ErrorActionPreference = 'Stop'

            gcloud config set project $env:GCP_PROJECT

            gcloud container clusters get-credentials `
              $env:GKE_CLUSTER `
              --zone $env:GKE_ZONE

            $image = "$env:IMAGE_REPOSITORY`:latest"

            kubectl set image deployment/app `
              petmedtrack-app=$image `
              --namespace $env:K8S_NAMESPACE

            kubectl rollout restart deployment/app `
              --namespace $env:K8S_NAMESPACE

            kubectl rollout status deployment/app `
              --namespace $env:K8S_NAMESPACE `
              --timeout=180s
        '''
    }
}
    }
}
