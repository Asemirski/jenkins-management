import jenkins.model.*
import hudson.node_monitors.DiskSpaceMonitorDescriptor
import java.math.RoundingMode

def jenkins = Jenkins.instance

// Function to convert bytes to MB and calculate free percentage
def getDiskStats(total, free) {
    if (total <= 0) return ['Unknown', 'Unknown', 'Unknown']

    def totalMB = BigDecimal.valueOf(total / (1024.0 * 1024)).setScale(2, RoundingMode.HALF_UP)
    def freeMB = BigDecimal.valueOf(free / (1024.0 * 1024)).setScale(2, RoundingMode.HALF_UP)
    def percentFree = BigDecimal.valueOf((free * 100.0) / total).setScale(2, RoundingMode.HALF_UP)

    return [totalMB, freeMB, percentFree]
}

// Fetch disk space for the Jenkins Master node
def masterPath = jenkins.getRootDir()
def (masterTotalMB, masterFreeMB, masterPercentFree) = getDiskStats(masterPath?.totalSpace ?: 0, masterPath?.usableSpace ?: 0)

println 'Jenkins Master Node:'
println "   Total Space: ${masterTotalMB} MB"
println "   Free Space: ${masterFreeMB} MB"
println "   Free Percentage: ${masterPercentFree}%"

// Fetch disk space for all agent nodes
println '\nAgent Disk Space:'
jenkins.nodes.each { node ->
    def rootPath = node.getRootPath()
    if (rootPath) {
        def (agentTotalMB, agentFreeMB, agentPercentFree) = getDiskStats(rootPath.getTotalDiskSpace(), rootPath.getUsableDiskSpace())

        println "${node.displayName}:"
        println "   Total Space: ${agentTotalMB} MB"
        println "   Free Space: ${agentFreeMB} MB"
        println "   Free Percentage: ${agentPercentFree}%"
    } else {
        println "${node.displayName}: Unable to determine disk space (Agent offline?)"
    }
}
