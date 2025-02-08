import jenkins.model.*
import jenkins.branch.*
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import com.cloudbees.hudson.plugins.folder.Folder

// === CONFIGURATION ===
def directoryName = 'terragrunt-patterns' // Change this to your actual directory name
def listOnly = true                       // Set to false to delete disabled jobs

def jenkins = Jenkins.instance
def directory = jenkins.getItem(directoryName)

if (directory && directory instanceof Folder) {
    def disabledJobs = directory.getItems()
        .findAll { it instanceof WorkflowMultiBranchProject }
        .collectMany { multibranchPipeline ->
            multibranchPipeline.getItems().findAll { job -> job instanceof WorkflowJob && job.isDisabled() }
        }
    if (disabledJobs.isEmpty()) {
        println 'No disabled jobs found.'
    } else {
        println "Found ${disabledJobs.size()} disabled job(s):"
        disabledJobs.eachWithIndex { job, index -> println "  ${index + 1}. ${job.fullName}" }

        if (!listOnly) {
            println 'Deleting disabled jobs...'
            disabledJobs.each { job ->
                try {
                    job.delete()
                    println "  Deleted: ${job.fullName}"
                } catch (Exception e) {
                    println "  Failed to delete: ${job.fullName}, Error: ${e.message}"
                }
            }
        } else {
            println 'List-only mode enabled. No jobs were deleted.'
        }
    }
} else {
    println "Directory '${directoryName}' not found or is not a folder."
}
