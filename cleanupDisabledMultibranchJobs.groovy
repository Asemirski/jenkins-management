import jenkins.model.*
import jenkins.branch.*
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import com.cloudbees.hudson.plugins.folder.Folder

def directoryName = "your-directory-name" // Change this to your actual directory name

def jenkins = Jenkins.instance
def directory = jenkins.getItem(directoryName)

if (directory && directory instanceof Folder) {
    directory.getItems().each { job ->
        if (job instanceof WorkflowMultiBranchProject) {
            println "Multibranch Pipeline: ${job.fullName}"
            def jobsToDelete = []

            job.getItems().each { branchJob ->
                if (branchJob instanceof WorkflowJob && branchJob.isDisabled()) {
                    println "  Deleting Disabled Job: ${branchJob.fullName}"
                    jobsToDelete.add(branchJob)
                }
            }


            jobsToDelete.each { branchJob ->
                try {
                    branchJob.delete()
                    println "  Successfully deleted: ${branchJob.fullName}"
                } catch (Exception e) {
                    println "  Failed to delete: ${branchJob.fullName}, Error: ${e.message}"
                }
            }
        }
    }
} else {
    println "Directory '${directoryName}' not found or is not a folder."
}
