import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import hudson.util.Secret

def allStores = CredentialsProvider.lookupStores(Jenkins.instance)

allStores.each { store ->
    store.getDomains().each { domain ->
        def creds = store.getCredentials(domain)
        creds.each { cred ->
            println "ID: ${cred.id} (${cred.class.name})"
            cred.properties.each { prop ->
                def name = prop.key
                def value = prop.value

                try {
                    def resolvedValue = value
                    if (value instanceof Secret) {
                        resolvedValue = value.getPlainText()
                    } else if (value?.metaClass?.respondsTo(value, "getPlainText")) {
                        resolvedValue = value.getPlainText()
                    }
                    println "  ${name}: ${resolvedValue}"
                } catch (Exception e) {
                    println "  ${name}: [error reading value: ${e.message}]"
                }
            }
            println "-----"
        }
    }
}
