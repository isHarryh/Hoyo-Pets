/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets.animations;

import cn.harryh.hoyopets.HoyoConfig;
import cn.harryh.hoyopets.animations.AnimClip.AnimStage;
import cn.harryh.hoyopets.animations.AnimClip.AnimType;

import java.util.*;

import static cn.harryh.hoyopets.Const.behaviorBaseWeight;


public class GeneralBehavior extends Behavior {
    protected AnimStage stageCur;
    protected AnimClipGroup stageAnimList;
    protected Iterator<AnimStage> stageItr;
    protected final ArrayList<AnimStage> stageList;
    protected final HashMap<AnimStage, AnimClipGroup> stageAnimMap;
    protected final HashMap<AnimStage, AnimDataWeight[]> stageAnimWeightMap;

    public GeneralBehavior(HoyoConfig config, AnimClipGroup animList) {
        super(config, animList);

        stageAnimMap = anim_list.clusterByStage();
        stageAnimWeightMap = new HashMap<>();
        for (AnimStage key : stageAnimMap.keySet()) {
            AnimDataWeight[] temp = getActionList(stageAnimMap.get(key));
            if (temp.length > 0)
                stageAnimWeightMap.put(key, temp);
        }

        stageList = new ArrayList<>(stageAnimWeightMap.keySet().stream().toList());
        stageList.sort(Comparator.comparing(AnimStage::id));
        if (stageList.isEmpty())
            throw new NoSuchElementException("Animation stage map was empty because no animation's name was matched.");
        stageItr = stageList.iterator();

        action_list = new AnimDataWeight[0];
        nextStage();
    }

    public void nextStage() {
        if (!stageItr.hasNext())
            stageItr = stageList.iterator();
        stageCur = stageItr.next();
        stageAnimList = stageAnimMap.get(stageCur);
        action_list = stageAnimWeightMap.get(stageCur);
        autoCtrlReset();
    }

    public Set<AnimStage> getStages() {
        return stageAnimMap.keySet();
    }

    public AnimStage getCurrentStage() {
        return stageCur;
    }

    private AnimDataWeight[] getActionList(AnimClipGroup animList) {
        ArrayList<AnimDataWeight> actionList = new ArrayList<>(List.of(
                new AnimDataWeight(
                        animList.getLoopAnimData(AnimType.IDLE, AnimType.EMOJI_IDLE),
                        Math.round(behaviorBaseWeight / (float) Math.sqrt(config.behavior_ai_activation))
                )
        ));
        actionList.removeIf(e -> e.anim().isEmpty());
        return actionList.toArray(new AnimDataWeight[0]);
    }

    @Override
    public AnimData defaultAnim() {
        return stageAnimList.getLoopAnimData(AnimType.IDLE, AnimType.EMOJI_IDLE);
    }

    @Override
    public AnimData clickEnd() {
        return defaultAnim();
    }

    @Override
    public AnimData dropped() {
        return clickEnd();
    }
}
