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

                        if ([string]::IsNullOrWhiteSpace($env:DOCKERHUB_USERNAME)) { throw 'Docker Hub username was not bound' }
                        if ([string]::IsNullOrWhiteSpace($env:DOCKERHUB_TOKEN)) { throw 'Docker Hub token was not bound' }
                        Write-Output "Docker Hub username bound: $env:DOCKERHUB_USERNAME"
                        Write-Output "Docker Hub token length: $($env:DOCKERHUB_TOKEN.Length)"

                        $loginInfo = New-Object System.Diagnostics.ProcessStartInfo
                        $loginInfo.FileName = 'docker'
                        $loginInfo.Arguments = "login --username $env:DOCKERHUB_USERNAME --password-stdin"
                        $loginInfo.UseShellExecute = $false
                        $loginInfo.RedirectStandardInput = $true
                        $loginInfo.RedirectStandardOutput = $true
                        $loginInfo.RedirectStandardError = $true
                        $loginProcess = New-Object System.Diagnostics.Process
                        $loginProcess.StartInfo = $loginInfo
                        [void]$loginProcess.Start()
                        $loginProcess.StandardInput.Write($env:DOCKERHUB_TOKEN)
                        $loginProcess.StandardInput.Close()
                        $loginOutput = $loginProcess.StandardOutput.ReadToEnd()
                        $loginError = $loginProcess.StandardError.ReadToEnd()
                        $loginProcess.WaitForExit()
                        if ($loginProcess.ExitCode -ne 0) { throw "Docker Hub login failed: $loginError" }
                        Write-Output $loginOutput

                        docker build -t $appTag -t $appLatest .
                        if ($LASTEXITCODE -ne 0) { throw 'Application image build failed' }
                        docker build -t $notificationTag -t $notificationLatest ./notification-service
                        if ($LASTEXITCODE -ne 0) { throw 'Notification image build failed' }
                        docker push $appTag
                        if ($LASTEXITCODE -ne 0) { throw 'Application image push failed' }
                        docker push $appLatest
                        if ($LASTEXITCODE -ne 0) { throw 'Application latest image push failed' }
                        docker push $notificationTag
                        if ($LASTEXITCODE -ne 0) { throw 'Notification image push failed' }
                        docker push $notificationLatest
                        if ($LASTEXITCODE -ne 0) { throw 'Notification latest image push failed' }
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
