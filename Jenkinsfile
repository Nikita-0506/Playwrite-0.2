pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'Java21'
    }

    triggers {
        // Poll GitHub every 15 minutes for changes
        pollSCM('H/15 * * * *')
        
        // Alternatively, use cron for scheduled builds regardless of changes:
        // cron('H 9,17 * * 1-5')  // Run at 9 AM and 5 PM on weekdays
    }

    parameters {
        choice(name: 'BROWSER', choices: ['Chrome', 'Firefox'], description: 'Select browser to run tests')
        choice(name: 'ENV', choices: ['QA', 'DEV', 'PROD'], description: 'Select environment')
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright browsers...'
                bat '''
                    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
                '''
            }
        }

        stage('Run Tests') {
            steps {
                echo '🧪 Running Playwright tests...'
                echo "Browser: ${params.BROWSER}"
                echo "Environment: ${params.ENV}"
                script {
                    // Capture the exit code
                    def testResult = bat(script: "mvn clean test -Dbrowser=${params.BROWSER} -Denv=${params.ENV}", returnStatus: true)
                    
                    // Explicitly fail if tests failed
                    if (testResult != 0) {
                        currentBuild.result = 'FAILURE'
                        error("❌ Tests failed! Stopping pipeline.")
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'
                // Your deployment steps here
            }
        }
    }

    post {
        always {
            echo '📊 Publishing test results and reports...'

            // Publish JUnit XML test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            // Publish Cucumber HTML Report (if it exists)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'index.html',
                reportName: 'Cucumber HTML Report',
                reportTitles: 'Test Report'
            ])

            // Publish Surefire HTML Report
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire HTML Report',
                reportTitles: 'Surefire Report'
            ])

            // Publish ExtentReports (Spark Reports)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output',
                reportFiles: '**/Report/TestRunReport.html',
                reportName: 'ExtentReports',
                reportTitles: 'Extent Test Report'
            ])

            // Archive all reports as artifacts
            archiveArtifacts artifacts: '**/target/cucumber-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/surefire-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/test-output/**/*.*', allowEmptyArchive: true

            // Clean workspace after build
            cleanWs(cleanWhenNotBuilt: false,
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [[pattern: 'target/**', type: 'INCLUDE']])
        }
        success {
            echo '✅ Tests passed successfully!'
            // Uncomment below to enable email notifications
            // emailext(
            //     subject: "✅ Jenkins Build SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: """
            //         Build Status: SUCCESS
            //         Job: ${env.JOB_NAME}
            //         Build Number: ${env.BUILD_NUMBER}
            //         Browser: ${params.BROWSER}
            //         Environment: ${params.ENV}
            //         
            //         View Reports: ${env.BUILD_URL}
            //     """,
            //     to: 'ndpachkate17801@gmail.com',
            //     attachLog: false
            // )
        }
        failure {
            echo '❌ Tests failed! Pipeline stopped.'
            // Uncomment below to enable email notifications
            // emailext(
            //     subject: "❌ Jenkins Build FAILED - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: """
            //         Build Status: FAILED
            //         Job: ${env.JOB_NAME}
            //         Build Number: ${env.BUILD_NUMBER}
            //         Browser: ${params.BROWSER}
            //         Environment: ${params.ENV}
            //         
            //         Check Console Output: ${env.BUILD_URL}console
            //     """,
            //     to: 'ndpachkate17801@gmail.com',
            //     attachLog: true
            // )
        }
        unstable {
            echo '⚠️ Build is unstable!'
        }
    }
}