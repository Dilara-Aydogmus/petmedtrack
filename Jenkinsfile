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

        stage('Build') {
            steps {
                powershell '''
                    mvn -B clean package

                    if ($LASTEXITCODE -ne 0) {
                        throw "Main application build failed"
                    }
                '''

                dir('notification-service') {
                    powershell '''
                        mvn -B clean package

                        if ($LASTEXITCODE -ne 0) {
                            throw "Notification service build failed"
                        }
                    '''
                }
            }
        }


        stage('Docker Build and Push') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'dockerhub-token-v2',
                        variable: 'DOCKERHUB_TOKEN'
                    )
                ]) {

                    powershell '''
                        $appImage = "${env:IMAGE_REPOSITORY}:$env:BUILD_NUMBER"
                        $notificationImage = "${env:NOTIFICATION_IMAGE_REPOSITORY}:$env:BUILD_NUMBER"


                        Write-Output "Logging into Docker Hub..."

                        $env:DOCKERHUB_TOKEN | docker login `
                            --username dilaraydgms `
                            --password-stdin

                        if ($LASTEXITCODE -ne 0) {
                            throw "Docker Hub login failed"
                        }


                        Write-Output "Building main application image..."

                        docker build `
                            -t $appImage `
                            .

                        if ($LASTEXITCODE -ne 0) {
                            throw "Main application Docker build failed"
                        }


                        Write-Output "Building notification service image..."

                        docker build `
                            -t $notificationImage `
                            ./notification-service

                        if ($LASTEXITCODE -ne 0) {
                            throw "Notification Docker build failed"
                        }


                        Write-Output "Pushing main application image..."

                        docker push $appImage

                        if ($LASTEXITCODE -ne 0) {
                            throw "Main application Docker push failed"
                        }


                        Write-Output "Pushing notification service image..."

                        docker push $notificationImage

                        if ($LASTEXITCODE -ne 0) {
                            throw "Notification Docker push failed"
                        }


                        docker logout
                    '''
                }
            }
        }


        stage('Deploy to GKE') {
            steps {
                powershell '''
                    $appImage = "${env:IMAGE_REPOSITORY}:$env:BUILD_NUMBER"
                    $notificationImage = "${env:NOTIFICATION_IMAGE_REPOSITORY}:$env:BUILD_NUMBER"


                    Write-Output "Selecting GCP project..."

                    gcloud config set project $env:GCP_PROJECT

                    if ($LASTEXITCODE -ne 0) {
                        throw "GCP project configuration failed"
                    }


                    Write-Output "Connecting to GKE cluster..."

                    gcloud container clusters get-credentials `
                        $env:GKE_CLUSTER `
                        --zone $env:GKE_ZONE

                    if ($LASTEXITCODE -ne 0) {
                        throw "GKE connection failed"
                    }


                    Write-Output "Deploying main application..."

                    kubectl set image deployment/app `
                        petmedtrack-app=$appImage `
                        --namespace $env:K8S_NAMESPACE

                    if ($LASTEXITCODE -ne 0) {
                        throw "Main application deployment failed"
                    }


                    Write-Output "Deploying notification service..."

                    kubectl set image deployment/notification-service `
                        notification-service=$notificationImage `
                        --namespace $env:K8S_NAMESPACE

                    if ($LASTEXITCODE -ne 0) {
                        throw "Notification deployment failed"
                    }


                    Write-Output "Waiting for main application rollout..."

                    kubectl rollout status deployment/app `
                        --namespace $env:K8S_NAMESPACE `
                        --timeout=180s

                    if ($LASTEXITCODE -ne 0) {
                        throw "Main application rollout failed"
                    }


                    Write-Output "Waiting for notification service rollout..."

                    kubectl rollout status deployment/notification-service `
                        --namespace $env:K8S_NAMESPACE `
                        --timeout=180s

                    if ($LASTEXITCODE -ne 0) {
                        throw "Notification rollout failed"
                    }
                '''
            }
        }
    }


    post {
        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}
