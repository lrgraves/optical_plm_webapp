package com.eleoptics.spark.cache;

import com.eleoptics.spark.api.Asset;

import java.util.Collection;
import java.util.List;

public interface AssetDAO {

    public Collection<Asset> getAllAssets(String userID);

    public void putAllAssets(List<Asset> assetList, String userID);

    public Collection<Asset> getTopLevelProjects(String userID);

    public Collection<Asset> getDesignPathsForProject(String userID, String parentKey);

    public Collection<Asset> getFilesForDesignPath(String userID, String parentKey);

    public Asset getAsset(String userID, String assetKey);

    public Boolean isEmpty(String userID);

    public void emptyDAO(String userID);

}
