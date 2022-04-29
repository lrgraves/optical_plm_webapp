package com.eleoptics.spark.cache;

import com.eleoptics.spark.api.Asset;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class AssetDAOImpl implements AssetDAO {

    public AssetDAOImpl(RedissonClient redissonClient){
        redis = redissonClient;
    }

    RedissonClient redis;


    @Override
    public Collection<Asset> getAllAssets(String userID) {
        Map<String, Asset> allAssetMap = redis.getMap(getAllAssetsKey(userID));

        return allAssetMap.values();
    }


    @Override
    public void putAllAssets(List<Asset> assetList, String userID) {

        RMap<String, Asset> allAssetMap = redis.getMap(getAllAssetsKey(userID));

        RMap<String, Asset> allProjectsMap = redis.getMap(getProjectsKey(userID));
        RMap<String, Collection<Asset>> allDesignPathsMap = redis.getMap(getDesignPathsParentKey(userID));
        RMap<String, Collection<Asset>> allFilesMap = redis.getMap(getFilesParentKey(userID));

        assetList.forEach(asset -> {
            allAssetMap.putIfAbsent(asset.key, asset);
        });

        // Set parent projects map and get return of remaining files
        Collection<Asset> designPathsPlusFiles = putParentProjects(assetList, allProjectsMap);

        log.info("non project assets are {}", designPathsPlusFiles.stream().toString());

        // Set design paths and get return of only file assets
        Collection<Asset> fileAssets = putDesignPaths(designPathsPlusFiles, allDesignPathsMap);

        // Set files map
        //putFiles(fileAssets, allFilesMap);

        // Set expiration for all maps
        allAssetMap.expire(secondsToMidnight(), TimeUnit.SECONDS);
        allProjectsMap.expire(secondsToMidnight(), TimeUnit.SECONDS);
        allDesignPathsMap.expire(secondsToMidnight(), TimeUnit.SECONDS);
        allFilesMap.expire(secondsToMidnight(), TimeUnit.SECONDS);

    }


    @Override
    public Collection<Asset> getTopLevelProjects(String userID) {
        RMap<String, Asset> allProjectsMap = redis.getMap(getProjectsKey(userID));

        return  allProjectsMap.values();
    }


    @Override
    public Collection<Asset> getDesignPathsForProject(String userID, String parentKey) {
        RMap<String, Collection<Asset>> allDesignPathsMap = redis.getMap(getDesignPathsParentKey(userID));

        // Fetch only the design path collection that matches the parent key
        return allDesignPathsMap.get(parentKey);
    }

    @Override
    public Collection<Asset> getFilesForDesignPath(String userID, String parentKey) {
        RMap<String, Collection<Asset>> allFilesMap = redis.getMap(getFilesParentKey(userID));

        return  allFilesMap.get(parentKey);
    }

    @Override
    public Asset getAsset(String userID, String assetKey) {
        Map<String, Asset> allProjectsMap = redis.getMap(getProjectsKey(userID));
        Map<String, Asset> allDesignPathsMap = redis.getMap(getProjectsKey(userID));
        Map<String, Asset> allFilesMap = redis.getMap(getProjectsKey(userID));

        if(allProjectsMap.containsKey(assetKey)){
            return allProjectsMap.get(assetKey);
        }
        else if( allDesignPathsMap.containsKey(assetKey)){
            return allDesignPathsMap.get(assetKey);
        }else if( allFilesMap.containsKey(assetKey)){
            return  allFilesMap.get(assetKey);
        } else{
            return null;
        }

    }

    @Override
    public Boolean isEmpty(String userID) {
        Map<String, Asset> allAssetMap = redis.getMap(getProjectsKey(userID));
        return allAssetMap.isEmpty();
    }

    @Override
    public void emptyDAO(String userID) {
        Map<String, Asset> allAssetMap = redis.getMap(getAllAssetsKey(userID));
        Map<String, Asset> allProjectsMap = redis.getMap(getProjectsKey(userID));
        Map<String, Collection<Asset>> allDesignPathsMap = redis.getMap(getProjectsKey(userID));
        Map<String, Collection<Asset>> allFilesMap = redis.getMap(getProjectsKey(userID));

        allAssetMap.clear();
        allProjectsMap.clear();
        allDesignPathsMap.clear();
        allFilesMap.clear();
    }

    protected Collection<Asset> putParentProjects(Collection<Asset> fullAssetList, Map<String, Asset> projectMap){
        List<Asset> projectsList = Asset.getAllProjects((List<Asset>) fullAssetList);

        Collection<Asset> assetList = fullAssetList;

        projectsList.forEach(asset -> {
            projectMap.putIfAbsent(asset.key, asset);

            assetList.remove(asset);

        });

       return assetList;
    }

    protected Collection<Asset> putDesignPaths(Collection<Asset> fullAssetList, Map<String, Collection<Asset>> designPathMap){

        // Get all keys that have children with matching parent keys
        List<String> assetKeys = new ArrayList<>();
        List<String> parentKeys = new ArrayList<>();
        Map<String, Asset> fullAssetMap = new HashMap<>();

        fullAssetList.forEach(asset -> {
            assetKeys.add(asset.key);
            parentKeys.add(asset.parentKey);

            //For the return, we want a collection of only asset files.
            fullAssetMap.put(asset.key, asset);
        });

        assetKeys.forEach(key -> {
            if(parentKeys.contains(key)){
                // then the asset is a design path, not a bottom level file
                // thus, add it to design paths collection
                Asset tempDesignPath = fullAssetMap.remove(key);
                log.info("An asset is being put in with name {}", tempDesignPath);

                // If the design path map doesnt have the parent key in the map, then make a new list and add the current
                // design path asset to a collection and drop that into the hashmp
                if (!designPathMap.containsKey(tempDesignPath.parentKey)) {
                    Collection<Asset> list = new ArrayList<Asset>();
                    list.add(tempDesignPath);

                    designPathMap.put(tempDesignPath.parentKey, list);
                } else {
                    designPathMap.get(tempDesignPath.parentKey).add(tempDesignPath);
                }
            }
        });

        return fullAssetMap.values();

    }

    protected  void putFiles(Collection<Asset> filesCollection, Map<String, Asset> filesMap){
        filesCollection.forEach(asset ->{
            filesMap.put(asset.key, asset);
        });
    }

    protected String getAllAssetsKey(String userID){
        return CacheMapNames.allAssetsKey + ":" + userID;
    }

    protected String getProjectsKey(String userID){
        return CacheMapNames.allProjectsKey + ":" + userID;
    }

    protected String getDesignPathsParentKey(String userID){
        return CacheMapNames.allDesignPathsParentKey + ":" + userID ;
    }

    protected String getFilesParentKey(String userID){
        return CacheMapNames.allFilesParentKey + ":" + userID;
    }

    protected Long secondsToMidnight(){
        LocalDateTime midnightTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime nowTime = LocalDateTime.now();
        long seconds = nowTime.until(midnightTime, ChronoUnit.SECONDS);

        return seconds;
    }

}
