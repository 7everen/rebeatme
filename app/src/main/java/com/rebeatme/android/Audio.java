package com.rebeatme.android;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Audio {
    public String audioBitsString = "0.4527891 0.9636281 1.4860771 2.008526 2.542585 3.065034" +
            " 3.587483 4.109932 4.632381 5.15483 5.665669 6.188118" +
            " 6.710567 7.2330155 7.7554646 8.266304 8.788753 9.299592" +
            " 9.822041 10.34449 10.866939 11.400997 11.923447 12.434285" +
            " 12.956735 13.479183 14.001633 14.524081 15.046531 15.557369" +
            " 16.068209 16.590658 17.113106 17.647165 18.169615 18.680452" +
            " 19.202902 19.736961 20.2478 20.770248 21.281088 21.803537" +
            " 22.314377 22.836824 23.359274 23.881723 24.404171 24.93823" +
            " 25.46068 25.98313 26.493967 27.016417 27.538866 28.061316" +
            " 28.572153 29.094603 29.617052";

    public List<String> audioBitsStringList = Arrays.asList(audioBitsString.split(" "));

    public List<Double> audioBits;

    public Audio() {
        audioBits = audioBitsStringList.stream()
                .map(Double::parseDouble)
                .collect(Collectors.toList());

    }

}
