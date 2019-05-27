package io.choerodon.devops.infra.feign.fallback;

import java.util.Map;

import io.choerodon.devops.api.dto.sonar.*;
import io.choerodon.devops.infra.feign.SonarClient;
import retrofit2.Call;

/**
 * Created by Sheep on 2019/5/6.
 */
public class SonarClientFallback implements SonarClient {

    @Override
    public Call<SonarComponent> getSonarComponet(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<SonarAnalyses> getAnalyses(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Bug> getBugs(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Vulnerability> getVulnerability(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Bug> getNewBugs(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Vulnerability> getNewVulnerability(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<SonarTables> getSonarTables(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> updateVisibility(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> updateDefaultVisibility(Map<String, String> maps){
        return null;
    }

    @Override
    public Call<Void> addGroupToTemplate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> removeGroupFromTemplate(Map<String, String> maps) {return null;}

    @Override
    public Call<Projects> listProject() {
        return null;
    }

}
