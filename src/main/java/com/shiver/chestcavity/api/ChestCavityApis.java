package com.shiver.chestcavity.api;

/**
 * 汇总对外开放的全部胸腔运行时 API。
 */
public final class ChestCavityApis {

    public static final OrganDataApi ORGANS = new OrganDataApi();
    public static final ChestCavityTypeApi TYPES = new ChestCavityTypeApi();
    public static final EntityAssignmentApi ENTITY_ASSIGNMENTS = new EntityAssignmentApi();
    public static final DropApi DROPS = new DropApi();
    public static final ScoreApi SCORES = new ScoreApi();
    public static final AbilityApi ABILITIES = new AbilityApi();
    public static final ChestCavityAccess CHEST_CAVITIES = new ChestCavityAccess();

    /**
     * 工具类，不允许外部实例化。
     */
    private ChestCavityApis() {
    }
}
