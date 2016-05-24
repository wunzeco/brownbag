/*
    Note: To access Jenkins env variables (such as WORKSPACE, BUILD_NUMBER)
           from within DSL scripts just wrap them in '${}'.
*/
if ( !Object.metaClass.hasProperty('BUILD_NUMBER') ) {
    BUILD_NUMBER = '1.0'
} 

print "Build number is => ${BUILD_NUMBER}"

/*
    service/component build job
*/
def svc = 'data_loader'
def rakeScriptsRepo = 'infra-rake-tf-build'
def productDslRepo = 'infra-priority-stack'
job("${svc}-service-build") {
    scm {
        git {
            remote {
                github("wunzeco/brownbag")
            }
        }
    }
    triggers {
        scm('H/2 * * * *')
    }
    steps {
        shell ( "echo 'COMPILATION of code happens here'" )
        shell(
            "#!/bin/bash" +
            """
            source ~/development/bin/activate
            cd \$WORKSPACE/app
            pip install -r requirements.txt
            echo -e "\\n\\n****** STATIC CODE ANALYSIS" && pylint *.py
            echo -e "\\n\\n****** UNIT TEST" && nosetests -v
            """.stripIndent()
             )
        shell(
            """
            cd \$WORKSPACE
            rm -f *.tar.gz
            echo -e '****** PACKAGING'
            tar -zcvf ${svc}-service-\$BUILD_NUMBER.tar.gz app
            cp ${svc}-service-\$BUILD_NUMBER.tar.gz \$HOME/artifact-store/
            cp ${svc}-service-\$BUILD_NUMBER.tar.gz \$HOME/artifact-store/${svc}-service-latest.tar.gz
            """.stripIndent()
             )
    }
    wrappers {
        colorizeOutput()
    }
    publishers {
        archiveArtifacts {
            pattern("**/*.tar.gz")
            onlyIfSuccessful()
        }
        downstreamParameterized {
            trigger("${svc}-service-deploy") {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                    predefinedProp('DC_ENVIRONMENT', 'dev')
                    predefinedProp('APP_NAME', "${svc}-service")
                }
            }
        }
    }
}

/*
    svc deploy job
*/
job("${svc}-service-deploy") {
    parameters {
        stringParam('APP_NAME',     defaultValue = "${svc}", 
                    description = 'App Name')
    }         
    scm {
        git {
            remote {
                github("wunzeco/brownbag")
            }
        }
    }
    steps {
        shell(
            """
            cd \$WORKSPACE/ansible
            EXTRA_VARS="app_name=\$APP_NAME"
            #ansible-galaxy install -r requirements.yml -f -p galaxy_roles/
            ansible-playbook -i inventory playbooks/deploy.yml -e "\$EXTRA_VARS"
            """.stripIndent()
                )
    }
    wrappers {
        colorizeOutput()
    }
}

/*
    svc functional test job
*/
job("${svc}-service-test") {
    scm {
        git {
            remote {
                github("wunzeco/brownbag")
            }
        }
    }
    triggers {
        upstream("${svc}-service-deploy")
    }
    steps {
        shell(
            """
            export DL_HOST=172.20.10.20
            cd \$WORKSPACE/app
            bundle install && cucumber
            """.stripIndent()
             )
    }
}
