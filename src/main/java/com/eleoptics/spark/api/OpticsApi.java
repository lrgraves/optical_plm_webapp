package com.eleoptics.spark.api;

import com.eleoptics.spark.config.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleState;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Array;
import java.util.*;

@Slf4j
public class OpticsApi {
    @Autowired

    private RestTemplate restTemplate;

    @Service
    public class OpticsAPIConsumerService {

        private RestTemplate restTemplate;

        @Autowired
        public OpticsAPIConsumerService(RestTemplateBuilder restTemplateBuilder) {
            RestTemplate restTemplate = restTemplateBuilder
                    .errorHandler(new RestTemplateResponseHandler())
                    .build();
        }

    }

    public class SurfacePoints {
        Float xPoint;
        Float yPoint;
    }

    public Assets getProjects() {
        // define get asset url
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "v1alpha/assets";
        log.info("API Call: " + apiURL);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        ResponseEntity<Assets> response =
                restTemplate.exchange(
                        apiURL,
                        HttpMethod.GET,
                        new HttpEntity<>("parameters", headers),
                        Assets.class);
        log.info(response.toString().substring(0, 30));

        // This will directly return the assets list
        return response.getBody();

    }

    public Assets getSharedAsset(String assetId, Integer version, Boolean isRecursive, String apiURL) {
        apiURL = apiURL + "/v1alpha/share";

            //log.info("The version is" + version);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    apiURL + "?parent_id=" + assetId + "&parent_version=" + version
            + "&is_recursive=" + isRecursive);


            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("{}", builder.toUriString());
            log.info("{}", entity.toString());

            HttpEntity<Assets> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Assets.class);

            log.info("{}", response.toString());

            return response.getBody();
        }

    public Assets getAllAssets() {
        // define get asset url
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "v1alpha/assets?is_recursive=true";
        log.info("API URL: " + apiURL);


        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        RestTemplate template = restTemplate;
        template.setErrorHandler(new RestTemplateResponseHandler());

        HttpEntity<Assets> response = template.
                exchange(
                        apiURL,
                        HttpMethod.GET,
                        new HttpEntity<>("parameters", headers),
                        Assets.class);
        log.info(response.toString().substring(0, 30));

        // This will directly return the assets list, and handle errors
        Assets assetList = new Assets();

        assetList = response.getBody();


        return assetList;

    }

    public void resetPassword(String userEmail) {
        String resetURL = "https://dev-396vb1r5.us.auth0.com/dbconnections/change_password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.add("Authorization","Bearer "+SecurityUtils.getJWT());

        HashMap<String, String> map = new HashMap<>();


        map.put("client_id", "PhSFR64fSEnn3L4aDWKhAMD4guHoDJIj");
        map.put("email", userEmail.toString());
        map.put("connection", "Username-Password-Authentication");

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        resetURL,
                        request,
                        String.class);
        log.info("Password reset called, response: {}", response.toString().substring(0, 30));

    }

    public Assets downloadAllFiles(String parentId) {

        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";


        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        ResponseEntity<Assets> response =
                restTemplate.exchange(
                        apiURL + "?parent_id:" + parentId,
                        HttpMethod.GET,
                        new HttpEntity<>("parameters", headers),
                        Assets.class);
        //log.info(response.toString());

        // This will directly return the assets list
        return response.getBody();

    }

    public Assets getDesignPaths(String parentId, Integer version) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        //log.info("The version is" + version);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                apiURL + "?parent_id=" + parentId);

        if (version != null) {
            builder.queryParam("parent_version", version);
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString().substring(0,30));

        HttpEntity<Assets> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Assets.class);

        log.info("{}", builder.toUriString().substring(0, 30));

        return response.getBody();
    }

    public Assets getAllDesignPathAssets(String parentId) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        //log.info("The version is" + version);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                apiURL + "?parent_id=" + parentId + "&is_recursive=true");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        log.info("{}", builder.toUriString().substring(0, 30));

        HttpEntity<Assets> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Assets.class);

        log.info("return for design paths:" + response.toString().substring(0, 15));

        return response.getBody();
    }

    public Asset getProject(String assetKey, @Nullable Integer version) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("" +
                apiURL + "/" + assetKey);

        if (version != null) {
            builder.queryParam("version", version);
        }

        builder.queryParam("data", "true");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString());
        System.out.println(builder.toUriString());

        HttpEntity<Asset> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Asset.class);

        //log.info("return:" + response.getBody().toString());

        return response.getBody();
    }

    public Asset getProjectNoData(String assetKey, @Nullable Integer version) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiURL + "/" + assetKey);

        if (version != null) {
            builder.queryParam("version", version);
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString().substring(0,30));
        HttpEntity<Asset> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Asset.class);

        //log.info("return:" + response.getBody().toString().substring(0,30));

        return response.getBody();
    }

    public Asset getFile(String assetKey, @Nullable Integer version) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiURL + "/" + assetKey);

        if (version != null) {
            builder.queryParam("version", version);
        }

        builder.queryParam("data", "true");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString());
        System.out.println(builder.toUriString());

        HttpEntity<Asset> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Asset.class);

        //log.info("return:" + response.getBody().toString());

        return response.getBody();
    }

    public VisualizationData getVisualizationPoints(String assetKey, @Nullable Integer version, ArrayList<Integer> sampleList, @Nullable Integer numberOfRays) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets/visualize-2d";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        // URL REQUEST CREATION
        apiURL = apiURL + "?id=" + assetKey;
        if (version != null) {
            apiURL = apiURL + "&version=" + version;
        }
        if (numberOfRays != null) {
            apiURL = apiURL + "&number-of-rays=" + numberOfRays;
        }

        // remove whitespce

        apiURL = apiURL + "&samples=" + sampleList.toString().replaceAll("\\s", "");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString());
        log.info("Visualiation API call: " + entity);

        try {
            ResponseEntity<VisualizationData> response =
                    restTemplate.exchange(
                            apiURL,
                            HttpMethod.GET,
                            new HttpEntity<>("parameters", headers),
                            VisualizationData.class);

            log.info("return: {}" + response.toString());//.substring(0,200));

            return response.getBody();
        } catch (RestClientException returnError) {
            log.info(returnError.toString());
            return new VisualizationData();
        }


    }

    public VisualizationData publicVisualizationPoints(String assetKey, @Nullable Integer version, ArrayList<Integer> sampleList, @Nullable Integer numberOfRays, String apiURL) throws JsonProcessingException {

        apiURL = apiURL + "/v1alpha/share/visualize-2d";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // URL REQUEST CREATION
        apiURL = apiURL + "?id=" + assetKey;
        if (version != null) {
            apiURL = apiURL + "&version=" + version;
        }
        if (numberOfRays != null) {
            apiURL = apiURL + "&number-of-rays=" + numberOfRays;
        }

        // remove whitespce

        apiURL = apiURL + "&samples=" + sampleList.toString().replaceAll("\\s", "");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        //log.info("{}", builder.toUriString());
        log.info("Visualiation API call: " + entity);

        try {
            ResponseEntity<VisualizationData> response =
                    restTemplate.exchange(
                            apiURL,
                            HttpMethod.GET,
                            new HttpEntity<>("parameters", headers),
                            VisualizationData.class);

            log.info("return: {}" + response.toString());//.substring(0,200));

            return response.getBody();
        } catch (RestClientException returnError) {
            log.info(returnError.toString());
            return new VisualizationData();
        }


    }

    public Asset sendFile(AssetObjectForUpload assetObject, @Nullable AssetDataForProjectUpload assetData) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String assetObjectString = objectMapper.writeValueAsString(assetObject);

        map.add("asset_object", assetObjectString);
        map.add("asset_data", assetData.getFile());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        log.info("Request sent: " + request.toString());

        ResponseEntity<Asset> response =
                restTemplate.postForEntity(
                        apiURL, request, Asset.class);
        System.out.println(response.toString());
        log.info(response.toString());

        // This will directly return the assets list
        return response.getBody();
    }

    public void patchAsset(AssetObjectForUpdate assetObject, @Nullable AssetDataForProjectUpload assetData) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String assetObjectString = objectMapper.writeValueAsString(assetObject);

        log.info("The update is: " + assetObject.toString());

        map.add("asset_object", assetObjectString);
        map.add("asset_data", assetData.getFile());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        log.info("The entity is:" + request);

        String url = new String(apiURL + "/");

        HttpEntity<Asset> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                Asset.class);

        log.info("return:" + response.getBody().toString());

    }

    public void patchProject(AssetObjectForUpdate assetObject) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String assetObjectString = objectMapper.writeValueAsString(assetObject);


        System.out.println("The update is: " + assetObject.toString());

        //Create empty asset_data structure
        ProjectDataForProjectUpdate projectData = new ProjectDataForProjectUpdate();

        String jsonRequirements = new ObjectMapper().writeValueAsString(projectData);

        map.add("asset_object", assetObjectString);
        //map.add("asset_data", jsonRequirements);

        // log.info("The data is:" + jsonRequirements.substring(0,30));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        System.out.println("The entity is:" + request);

        String url = new String(apiURL + " /");

        HttpEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                String.class);

        log.info(request.toString());

        log.info(response.toString());

    }

    public Asset sendNewProject(ProjectForUpload assetObject, ProjectDataForProjectUpload projectData) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();


        ObjectMapper objectMapper = new ObjectMapper();
        String assetObjectString = objectMapper.writeValueAsString(assetObject);

        String jsonRequirements = new ObjectMapper().writeValueAsString(projectData);


        map.add("asset_object", assetObjectString);
        map.add("asset_data", jsonRequirements);


        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

        //log.info(request.toString());

        ResponseEntity<Asset> response =
                restTemplate.postForEntity(
                        apiURL, request, Asset.class);
        log.info(response.toString().substring(0, 30));

        // This will directly return the assets list
        return response.getBody();
    }



    public Asset sendNewDesignPath(DesignPathForUpload assetObject, ProjectDataForProjectUpload projectData)
            throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();


        ObjectMapper objectMapper = new ObjectMapper();
        String assetObjectString = objectMapper.writeValueAsString(assetObject);

        String jsonRequirements = new ObjectMapper().writeValueAsString(projectData);


        map.add("asset_object", assetObjectString);
        map.add("asset_data", jsonRequirements);


        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

        //log.info(request.toString());
        System.out.println(request.toString());

        ResponseEntity<Asset> response =
                restTemplate.postForEntity(
                        apiURL, request, Asset.class);
        //log.info(response.toString());
        System.out.println(response.toString());

        // This will directly return the assets list
        return response.getBody();
    }

    public void deleteAsset(String assetId, Integer version) {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "/v1alpha/assets";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());


        // This deletes all version
        // while (version >= 0) {


        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(headers);

        String deleteUrl = apiURL + "/" +
                assetId +
                "?version=" + version.toString();

        //  log.info("The delete url is" + deleteUrl);
        ResponseEntity<String> response =
                restTemplate.exchange(deleteUrl,
                        HttpMethod.DELETE,
                        new HttpEntity<>("parameters", headers),
                        String.class);

        //   version--;
        // }
    }

    public void makePublic(AssetsForSharing assetIds) throws JsonProcessingException {
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiURL = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() + "v1alpha/assets/public";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + SecurityUtils.getJWT());

        log.info("share url:" + apiURL);

        HttpEntity<String> request =
                new HttpEntity<String>(assetIds.toString(), headers);

        log.info(request.toString());

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        apiURL, request, String.class);
        log.info(response.toString());

        // This will directly return the assets list
    }



}
