pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'Java21'
    }

    triggers {
        pollSCM('*/5 * * * *')
    }

    parameters {
        choice(name: 'BROWSER', choices: ['Chrome', 'Firefox'], description: 'Select browser')
        choice(name: 'ENV', choices: ['QA', 'DEV', 'PROD'], description: 'Select environment')
        booleanParam(name: 'AUTO_MODE', defaultValue: true, description: 'Auto-select tags by branch (recommended)')
        choice(name: 'TAGS', choices: [
            '@SMOKE',
            '@REGRESSION',
            '@SANITY',
            '@SMOKE or @REGRESSION',
            '@SMOKE or @SANITY',
            '@REGRESSION or @SANITY',
            '@SMOKE or @REGRESSION or @SANITY'
        ], description: 'Cucumber tag expression to run')
    }

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'qa-accelerator-inadev'
        S3_PREFIX = 'all_reports'
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
                script {
                    env.BUILD_TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
                }
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright...'
                script {
                    if (isUnix()) {
                        sh 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    } else {
                        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    }
                }
            }
        }

        stage('Run Tests') {
            options {
                timeout(time: 60, unit: 'MINUTES')
                retry(1)
            }
            steps {
                echo "🧪 Running Tests on ${params.BROWSER} in ${params.ENV}"
                script {
                    String autoTags = '@SMOKE'
                    String branchName = (env.BRANCH_NAME ?: '').toLowerCase()

                    // Fully automated selection: feature branches run smoke, main/release run broader suites.
                    if (branchName == 'main' || branchName == 'master') {
                        autoTags = '@SMOKE or @REGRESSION or @SANITY'
                    } else if (branchName.startsWith('release/')) {
                        autoTags = '@SMOKE or @REGRESSION'
                    }

                    String selectedTags = params.AUTO_MODE ? autoTags : params.TAGS
                    env.SELECTED_TAGS = selectedTags
                    echo "🏷️ Tag Selection: ${selectedTags} (AUTO_MODE=${params.AUTO_MODE}, BRANCH=${env.BRANCH_NAME ?: 'N/A'})"

                    // -Dcucumber.filter.tags lets Jenkins control which scenarios run by tag.
                    // -Dmaven.test.failure.ignore=true ensures Maven always completes,
                    // writes all Surefire/Cucumber XML reports, and generates Extent Report
                    // even when tests fail — so downstream stages (S3 upload) always run.
                    def mvnCmd = "mvn clean verify -Dbrowser=${params.BROWSER} -Denv=${params.ENV} -Dcucumber.filter.tags=\"${selectedTags}\" " +
                                 "-Dmaven.test.failure.ignore=true -Dsurefire.failIfNoSpecifiedTests=false"

                    def status
                    if (isUnix()) {
                        status = sh(script: mvnCmd, returnStatus: true)
                    } else {
                        status = bat(script: mvnCmd, returnStatus: true)
                    }

                    // Parse Surefire XML to get actual test counts
                    // Uses powershell/sh instead of findFiles (no Pipeline Utility Steps plugin needed)
                    int totalTests = 0, failedTests = 0, skippedTests = 0
                    try {
                        def countOutput
                        if (isUnix()) {
                            countOutput = sh(returnStdout: true, script: '''
                                T=0; F=0; S=0
                                for f in target/surefire-reports/TEST-*.xml; do
                                    [ -f "$f" ] || continue
                                    t=$(grep -oP 'tests="\\K[0-9]+' "$f" | head -1); T=$((T + ${t:-0}))
                                    fail=$(grep -oP 'failures="\\K[0-9]+' "$f" | head -1); F=$((F + ${fail:-0}))
                                    err=$(grep -oP 'errors="\\K[0-9]+' "$f" | head -1); F=$((F + ${err:-0}))
                                    skip=$(grep -oP 'skipped="\\K[0-9]+' "$f" | head -1); S=$((S + ${skip:-0}))
                                done
                                echo "$T,$F,$S"
                            ''').trim()
                        } else {
                            countOutput = powershell(returnStdout: true, script: '''
                                $t=0; $f=0; $s=0
                                Get-ChildItem -Path "target\\surefire-reports" -Filter "TEST-*.xml" -ErrorAction SilentlyContinue | ForEach-Object {
                                    try {
                                        [xml]$xml = Get-Content $_.FullName -ErrorAction Stop
                                        $suite = $xml.testsuite
                                        $t += [int]($suite.tests)
                                        $f += [int]($suite.failures) + [int]($suite.errors)
                                        $s += [int]($suite.skipped)
                                    } catch {}
                                }
                                Write-Output "$t,$f,$s"
                            ''').trim()
                        }
                        if (countOutput?.contains(',')) {
                            def parts = countOutput.split(',')
                            totalTests  = parts[0].trim().toInteger()
                            failedTests = parts[1].trim().toInteger()
                            skippedTests = parts[2].trim().toInteger()
                        }
                    } catch (Exception parseEx) {
                        echo "⚠️ Could not parse test counts: ${parseEx.message}"
                    }

                    int passedTests = totalTests - failedTests - skippedTests

                    if (totalTests > 0) {
                        echo "📊 Test Results: ${passedTests} passed | ${failedTests} failed | ${skippedTests} skipped | ${totalTests} total"
                    }

                    if (status != 0 && totalTests == 0) {
                        // Maven failed before any tests ran → real build/compile error
                        currentBuild.result = 'FAILURE'
                        error("❌ Maven build failed — no tests were executed (possible compile error or plugin failure)")
                    } else if (failedTests > 0) {
                        // Tests ran but some failed → mark UNSTABLE so pipeline continues for report upload
                        unstable("⚠️ ${failedTests} test(s) failed out of ${totalTests} — reports will still be uploaded")
                    } else {
                        echo "✅ All ${passedTests} tests passed!"
                    }
                }
            }
        }

        // ✅ UPDATED S3 LOGIC (FROM CODE A)
        stage('Upload Reports to S3') {
            steps {
                echo '📤 Uploading reports to S3...'
                script {
                    try {
                        def awsCredStatus = isUnix()
                            ? sh(script: "aws sts get-caller-identity --region ${AWS_REGION} >/dev/null 2>&1", returnStatus: true)
                            : bat(script: "@aws sts get-caller-identity --region ${AWS_REGION} >NUL 2>&1", returnStatus: true)

                        if (awsCredStatus != 0) {
                            echo '⚠️ AWS credentials are not configured on this Jenkins agent. Skipping S3 upload stage.'
                            return
                        }

                        def shortCommit = isUnix()
                            ? sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
                            : bat(script: '@git rev-parse --short=7 HEAD', returnStdout: true).trim().readLines().last()
                        def folderName = "run${BUILD_NUMBER}_${shortCommit}_${BUILD_TIMESTAMP}"
                        def s3Path = "s3://${S3_BUCKET}/${S3_PREFIX}/${folderName}"

                        echo "📂 Folder: ${folderName}"

                        if (isUnix()) {
                            sh """
                                # Upload Surefire Reports
                                if [ -d target/surefire-reports ]; then
                                    aws s3 cp target/surefire-reports ${s3Path}/surefire-reports/ --recursive --exclude "old/*" --region ${AWS_REGION}
                                fi

                                # Upload Cucumber Reports
                                if [ -d target/cucumber-reports ]; then
                                    aws s3 cp target/cucumber-reports ${s3Path}/cucumber-reports/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload Latest Spark Report
                                if [ -d test-output ]; then
                                    LATEST=\$(find test-output -maxdepth 1 -type d -name "SparkReports*" | sort -r | head -1)
                                    if [ -n "\$LATEST" ]; then
                                        aws s3 cp "\$LATEST" ${s3Path}/extent-report/ --recursive --region ${AWS_REGION}
                                    fi
                                fi

                                # Upload Screenshots
                                if [ -d target/screenshots ]; then
                                    aws s3 cp target/screenshots ${s3Path}/screenshots/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload Videos
                                if [ -d target/videos ]; then
                                    aws s3 cp target/videos ${s3Path}/videos/ --recursive --region ${AWS_REGION}
                                fi

                                # Upload JaCoCo Report
                                if [ -d target/site/jacoco ]; then
                                    aws s3 cp target/site/jacoco ${s3Path}/jacoco/ --recursive --region ${AWS_REGION}
                                fi

                                # Metadata
                                cat > build-metadata.json <<EOF
{
  "build_number": "${BUILD_NUMBER}",
  "commit_id": "${shortCommit}",
  "timestamp": "${BUILD_TIMESTAMP}",
  "status": "${currentBuild.result ?: 'SUCCESS'}",
  "browser": "${params.BROWSER}",
  "environment": "${params.ENV}",
  "job": "${JOB_NAME}",
  "url": "${BUILD_URL}"
}
EOF
                                aws s3 cp build-metadata.json ${s3Path}/build-metadata.json --region ${AWS_REGION}
                            """
                        } else {
                            def buildStatus = currentBuild.result ?: 'SUCCESS'
                            writeFile file: 'build-metadata.json', text: """{
  \"build_number\": \"${BUILD_NUMBER}\",
  \"commit_id\": \"${shortCommit}\",
  \"timestamp\": \"${BUILD_TIMESTAMP}\",
  \"status\": \"${buildStatus}\",
  \"browser\": \"${params.BROWSER}\",
  \"environment\": \"${params.ENV}\",
  \"job\": \"${JOB_NAME}\",
  \"url\": \"${BUILD_URL}\"
}"""
                            bat """
                                if exist target\\surefire-reports aws s3 cp target/surefire-reports ${s3Path}/surefire-reports/ --recursive --exclude "old/*" --region ${AWS_REGION}
                                if exist target\\cucumber-reports aws s3 cp target/cucumber-reports ${s3Path}/cucumber-reports/ --recursive --region ${AWS_REGION}
                                if exist target\\screenshots aws s3 cp target/screenshots ${s3Path}/screenshots/ --recursive --region ${AWS_REGION}
                                if exist target\\videos aws s3 cp target/videos ${s3Path}/videos/ --recursive --region ${AWS_REGION}
                                if exist target\\site\\jacoco aws s3 cp target/site/jacoco ${s3Path}/jacoco/ --recursive --region ${AWS_REGION}
                                aws s3 cp build-metadata.json ${s3Path}/build-metadata.json --region ${AWS_REGION}
                            """
                            powershell """
                                \$latest = Get-ChildItem -Path 'test-output' -Filter 'SparkReports*' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
                                if (\$latest) {
                                    aws s3 cp \$latest.FullName '${s3Path}/extent-report/' --recursive --region ${AWS_REGION}
                                }
                            """
                        }

                        def url = "https://${S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com/${S3_PREFIX}/${folderName}"
                        echo "📊 Reports: ${url}"

                    } catch (Exception e) {
                        echo "⚠️ Upload failed: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo '🚀 Deploying application...'
            }
        }
    }

    post {
        always {
            echo '📊 Publishing Reports...'

            script {
                // Copy latest timestamped SparkReports folder to a fixed path for publishHTML
                if (isUnix()) {
                    sh '''
                        LATEST=$(find test-output -maxdepth 1 -type d -name "SparkReports*" 2>/dev/null | sort -r | head -1)
                        if [ -n "$LATEST" ]; then
                            rm -rf test-output/latest-report
                            cp -r "$LATEST" test-output/latest-report
                        fi
                    '''
                } else {
                    powershell '''
                        $latestDir = Get-ChildItem -Path "test-output" -Filter "SparkReports*" -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
                        if ($latestDir) {
                            if (Test-Path "test-output\\latest-report") { Remove-Item "test-output\\latest-report" -Recurse -Force }
                            Copy-Item $latestDir.FullName "test-output\\latest-report" -Recurse -Force
                        }
                    '''
                }
            }

            // Keep report publishing, but don't re-mark the build as UNSTABLE here.
            // Test outcome is already decided in the Run Tests stage.
            junit allowEmptyResults: true, skipPublishingChecks: true, skipMarkingBuildUnstable: true, testResults: '**/target/surefire-reports/*.xml'

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'cucumber.html,overview-features.html,feature-overview.html,index.html',
                reportName: 'Cucumber Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output/latest-report',
                reportFiles: 'Report/TestRunReport.html',
                reportName: 'Extent Report'
            ])

            archiveArtifacts artifacts: '**/target/**/*.*, **/test-output/**/*.*', allowEmptyArchive: true

            cleanWs()
        }

        success {
            echo '✅ Build SUCCESS'
        }

        failure {
            echo '❌ Build FAILED'
        }

        unstable {
            echo '⚠️ Build UNSTABLE'
        }
    }
}