pipeline {
    agent any

    environment {
        IMAGE_REPOSITORY = 'dilaraydgms/petmedtrack'
        NOTIFICATION_IMAGE_REPOSITORY = 'dilaraydgms/petmedtrack-notification'
        GCP_PROJECT = 'petmedtrack-510515'
        GKE_CLUSTER = 'petmedtrack-cluster'
        GKE_ZONE = 'us-central1-a'
        K8S_NAMESPACE = 'petmedtrack'
    }

    stages {
        stage('Build and test') {
            steps {
                powershell 'mvn -B clean package'
                dir('notification-service') {
                    powershell 'mvn -B clean package'
                }
            }
        }

        stage('Build and push Docker images') {
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

                        $appTag = "$env:IMAGE_REPOSITORY`:$env:BUILD_NUMBER"
                        $appLatest = "$env:IMAGE_REPOSITORY`:latest"
                        $notificationTag = "$env:NOTIFICATION_IMAGE_REPOSITORY`:$env:BUILD_NUMBER"
                        $notificationLatest = "$env:NOTIFICATION_IMAGE_REPOSITORY`:latest"

                        $env:DOCKERHUB_TOKEN |
                            docker login --username $env:DOCKERHUB_USERNAME --password-stdin

                        docker build -t $appTag -t $appLatest .
                        docker build -t $notificationTag -t $notificationLatest ./notification-service
                        docker push $appTag
                        docker push $appLatest
                        docker push $notificationTag
                        docker push $notificationLatest
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

                    $appImage = "$env:IMAGE_REPOSITORY`:latest"
                    $notificationImage = "$env:NOTIFICATION_IMAGE_REPOSITORY`:latest"

                    kubectl set image deployment/app `
                        petmedtrack-app=$appImage `
                        --namespace $env:K8S_NAMESPACE

                    kubectl set image deployment/notification-service `
                        notification-service=$notificationImage `
                        --namespace $env:K8S_NAMESPACE

                    kubectl rollout status deployment/app `
                        --namespace $env:K8S_NAMESPACE `
                        --timeout=180s

                    kubectl rollout status deployment/notification-service `
                        --namespace $env:K8S_NAMESPACE `
                        --timeout=180s
                '''
            }
        }
    }
}
