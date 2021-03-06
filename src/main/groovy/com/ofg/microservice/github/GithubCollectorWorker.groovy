package com.ofg.microservice.github

import com.ofg.infrastructure.discovery.ServiceResolver
import com.ofg.infrastructure.web.filter.correlationid.CorrelationIdHolder
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@TypeChecked
@Component
@PackageScope
@Slf4j
class GithubCollectorWorker implements GithubCollector {

    public static final String GITHUB_TOPICS_ANALYZER_CONTENT_TYPE_HEADER = "vnd.com.ofg.github-topics-analyzer.v1+json"
    public static
    final MediaType GITHUB_TOPICS_ANALYZER_MEDIA_TYPE = new MediaType('application', GITHUB_TOPICS_ANALYZER_CONTENT_TYPE_HEADER)

    private ReposGetter reposGetter
    private OrgsGetter orgsGetter
    private RestTemplate restTemplate = new RestTemplate()
    private ServiceResolver serviceResolver

    @Autowired
    GithubCollectorWorker(ReposGetter reposGetter, OrgsGetter orgsGetter, ServiceResolver serviceResolver) {
        this.reposGetter = reposGetter
        this.orgsGetter = orgsGetter
        this.serviceResolver = serviceResolver
    }

    void collectAndPassToAnalyzers(String githubLogin, String pairId) {
        List<Object> repos = reposGetter.getRepos(githubLogin)
        List<Object> orgs = orgsGetter.getOrgs(githubLogin)

        if (repos.size() == 0 && orgs.size() == 0) {
            return
        }

        GitHubData data = new GitHubData(
                [githubId: githubLogin,
                 pairId  : pairId,
                 repos   : repos,
                 orgs    : orgs])

        com.google.common.base.Optional<String> topicsUrlOpt = serviceResolver.getUrl('topics-analyzer')

        if (topicsUrlOpt.present) {
            try {
                String topicsUrl = topicsUrlOpt.get()
                restTemplate.postForLocation("${topicsUrl}/api/analyze", createTopicsEntity(data))
                log.info("Post to location ${topicsUrl}/api/analyze successfull")
            } catch (Exception e) {
                log.error(e.getMessage(), e)
                throw e
            }
        } else {
            log.warn("No analyzers found!")
        }
    }

    private HttpEntity<Object> createTopicsEntity(GitHubData gitHubData) {
        HttpHeaders headers = new HttpHeaders()

        headers.setContentType(GITHUB_TOPICS_ANALYZER_MEDIA_TYPE)
        headers.set(CorrelationIdHolder.CORRELATION_ID_HEADER, CorrelationIdHolder.get())

        return new HttpEntity<Object>(gitHubData, headers);
    }

}