/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets.animations;

import java.util.Objects;


/** Animation data record.
 * @param animClip The animation clip of THIS animation data.
 * @param animClipOver The animation clip of THIS animation data's upper track.
 * @param animNext The NEXT animation data, which would be applied after this animation ended.
 * @param isLoop {@code true} indicates that this animation could be played in loop.
 * @param isStrict {@code true} indicates that this animation couldn't be interrupted.
 * @param mobility The root motion. 0=None, 1=GoRight, -1=GoLeft.
 */
public record AnimData(
        AnimClip animClip,
        AnimClip animClipOver,
        AnimData animNext,
        boolean isLoop,
        boolean isStrict,
        int mobility
) {
    /** Animation data record (simplified constructor).
     * @param animClip The animation clip of THIS animation data.
     * @param animClipOver The animation clip of THIS animation data's upper track.
     */
    public AnimData(AnimClip animClip, AnimClip animClipOver) {
        this(animClip, animClipOver, null, false, false, 0);
    }

    /** Animation data record (simplified constructor).
     * @param animClip The animation clip of THIS animation data.
     * @param animClipOver The animation clip of THIS animation data's upper track.
     * @param animNext The NEXT animation data, which would be applied after this animation ended.
     * @param isLoop {@code true} indicates that this animation could be played in loop.
     * @param isStrict {@code true} indicates that this animation couldn't be interrupted.
     */
    public AnimData(AnimClip animClip, AnimClip animClipOver, AnimData animNext, boolean isLoop, boolean isStrict) {
        this(animClip, animClipOver, animNext, isLoop, isStrict, 0);
    }

    /** Derives a variation of this animation data by modifying the mobility property.
     * @param mobility New value for {@code mobility}.
     * @return New animation data.
     */
    public AnimData derive(int mobility) {
        return new AnimData(this.animClip, this.animClipOver, this.animNext, this.isLoop, this.isStrict, mobility);
    }

    /** Joins another animation data, which would be applied after this animation ended, to this animation data.
     * @param animNext The given animation data.
     * @return New animation data.
     */
    public AnimData join(AnimData animNext) {
        if (this.animNext == null)
            return new AnimData(this.animClip, this.animClipOver, animNext, this.isLoop, this.isStrict, this.mobility);
        else
            return new AnimData(this.animClip, this.animClipOver, this.animNext.join(animNext), this.isLoop, this.isStrict, this.mobility);
    }

    public boolean isEmpty() {
        return animClip == null;
    }

    public String name() {
        return isEmpty() ? null : animClip.fullName;
    }

    @Override
    public String toString() {
        return "AnimData {" + animClip + "}" +
                (isLoop ? " Loop" : "") +
                (isStrict ? " Strict" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimData animData = (AnimData) o;
        return isLoop == animData.isLoop && isStrict == animData.isStrict && mobility == animData.mobility && Objects.equals(animClip, animData.animClip) && Objects.equals(animNext, animData.animNext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animClip, animNext, isLoop, isStrict, mobility);
    }
}
