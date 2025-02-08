import jenkins.model.*
import jenkins.branch.*
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import com.cloudbees.hudson.plugins.folder.Folder

// === CONFIGURATION ===
def directoryName = 'your-directory-name' // Change this to your actual directory name
def buildsToKeep = 5                      // Adjust this number if needed
def listOnly = true                        // Set to false to delete builds

def jenkins = Jenkins.instance
def directory = jenkins.getItem(directoryName)

if (directory && directory instanceof Folder) {
    def jobsWithExcessBuilds = directory.getItems()
        .findAll { it instanceof WorkflowMultiBranchProject }
        .collectMany { pipelineJob ->
            pipelineJob.getItems()
                .findAll { it instanceof WorkflowJob && it.builds.size() > buildsToKeep }
        }

    if (jobsWithExcessBuilds.isEmpty()) {
        println "No jobs exceed the build limit (${buildsToKeep})."
    } else {
        println "Jobs with more than ${buildsToKeep} builds:"
        jobsWithExcessBuilds.eachWithIndex { job, index ->
            println "${index + 1}. ${job.fullName} (Total Builds: ${job.builds.size()})"
        }

        if (!listOnly) {
            println 'Deleting excess builds...'
            jobsWithExcessBuilds.each { job ->
                def buildsToDelete = job.builds.drop(buildsToKeep)
                println "Cleaning ${job.fullName} - Keeping last ${buildsToKeep} builds"
                buildsToDelete.each { build ->
                    try {
                        println "Deleting build: ${build.number} (${build.getTimestampString()})"
                        build.delete()
                    } catch (Exception e) {
                        println "Failed to delete build ${build.number}: ${e.message}"
                    }
                }
            }
        } else {
            println 'List-only mode enabled. No builds were deleted.'
        }
    }
} else {
    println "Directory '${directoryName}' not found or is not a folder."
}
