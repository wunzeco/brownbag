// If you want, you can define your seed job in the DSL and create it via the REST API.
// See README.md

/*
    _seed-microdc job definition
*/

job("_seed-microdc") {
    description("DSL seed job")
    scm {
        git {
            remote {
                github("wunzeco/brownbag")
            }
        }
    }
    triggers {
        scm 'H/2 * * * *'
    }
    steps {
        gradle {
            tasks('clean')
            tasks('test')
            rootBuildScriptDir('jenkins-job-dsl')
        }
        dsl {
            external('jenkins-job-dsl/jobs/**/*Jobs.groovy')
        }
        dsl { 
            external("jenkins-job-dsl/**/*Pipeline.groovy")
        }
    }
    publishers {
        archiveJunit 'jenkins-job-dsl/build/test-results/**/*.xml'
    }
}
