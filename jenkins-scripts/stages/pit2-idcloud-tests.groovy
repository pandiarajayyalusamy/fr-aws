/*
 * Copyright 2021 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

// pit2-idcloud-tests.groovy

import com.forgerock.pipeline.reporting.PipelineRunLegacyAdapter

void runStage(PipelineRunLegacyAdapter pipelineRun) {

    def stageName = 'PIT2 IDCloud'
    def normalizedStageName = dashboard_utils.normalizeStageName(stageName)

    pipelineRun.pushStageOutcome(normalizedStageName, stageDisplayName: stageName) {
        node('google-cloud') {
            stage(stageName) {
                def forgeopsPath = localGitUtils.checkoutForgeops()

                dir('lodestar') {
                    def stagesCloud = [:]
                    stagesCloud[normalizedStageName] = dashboard_utils.spyglaasStageCloud(normalizedStageName)

                    dashboard_utils.determineUnitOutcome(stagesCloud[normalizedStageName]) {
                       withCredentials([string(credentialsId: 'idc-tenant-passwprd', variable: 'TENANT_PASS')]) {
                            def config = [
                                TESTS_SCOPE                     : 'tests/pit1',
                                STASH_LODESTAR_BRANCH           : commonModule.LODESTAR_GIT_COMMIT,
                                EXT_FORGEOPS_PATH               : forgeopsPath,
                                REPORT_NAME_PREFIX              : normalizedStageName,
                                TENANT                          : 'openam-pitperf-tests',
                                TENANT_USER                     : 'pavel.balcarek@forgerock.com',
                                TENANT_PASSWORD                 : "${TENANT_PASS}"
                            ]

                            withGKESpyglaasNoStages(config)
                        }    
                    }

                    return dashboard_utils.finalLodestarOutcome(stagesCloud, stageName)
                }
            }
        }
    }
}

return this