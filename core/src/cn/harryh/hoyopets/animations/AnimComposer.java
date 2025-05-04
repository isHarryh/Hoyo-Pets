/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets.animations;

import com.esotericsoftware.spine.AnimationState;


public class AnimComposer {
    protected final AnimationState state;
    protected final int coreTrackId = 0;
    protected AnimData playing;

    public AnimComposer(AnimationState boundState) {
        AnimComposer composer = this;
        state = boundState;
        state.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if (composer.playing != null && !composer.playing.isEmpty() && entry.getAnimation() != null) {
                    if (entry.getAnimation().getName().equals(composer.playing.animClip().fullName)) {
                        AnimData completed = composer.playing;
                        if (!completed.isLoop()) {
                            composer.reset();
                            if (completed.animNext() != null && !completed.animNext().isEmpty()) {
                                composer.offer(completed.animNext());
                            }
                        }
                    }
                }
            }
        });
    }

    public boolean offer(AnimData animData) {
        if (animData != null && !animData.isEmpty()) {
            if (playing == null || playing.isEmpty() || (!playing.isStrict() && !playing.equals(animData))) {
                playing = animData;
                state.setAnimation(coreTrackId, playing.name(), playing.isLoop());
                onApply(playing);
                return true;
            }
        }
        return false;
    }

    public AnimData getPlaying() {
        return playing;
    }

    public void reset() {
        playing = null;
        state.setEmptyAnimation(coreTrackId, 0f);
    }

    protected void onApply(AnimData playing) {
    }

    @Override
    public String toString() {
        return "AnimComposer {" + playing + '}';
    }
}
