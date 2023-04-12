package io.scotthansen.workout.fixer;

import lombok.Builder;

import java.util.List;

@Builder
public class Data {
    List<TrackPoint> trackPointList;
    int totalTimeSeconds;

}
