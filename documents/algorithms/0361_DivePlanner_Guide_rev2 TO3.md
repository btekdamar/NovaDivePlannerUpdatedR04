{0}------------------------------------------------

# Pelagic Dive Planner - User Guide

## **Contents**

| Dive Settings 1<br>Gas Selection54<br>Adding Segments 75<br>Reading the Dive Plan 108<br>Printing Dive Information 119<br>Exporting Dive Information as a PDF File1210 |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

Planning a dive with Pelagic Dive Planner is generally done in three steps:A dive plan is done in three consecutive steps:

- 1. Selecting the dive settings
- 2. Selecting and setting the gases that will be used during dive
- 3. Adding the dive segments

<span id="page-0-0"></span>The following three paragraphs give details about each step.

# **2.1. Dive Settings**

All the settings of the dive are available in one single menu. To access the dive settings, click the Dive -> Dive Settings on the top menu.

## You may select:

## • Unit system: Metric or Imperial (1)

If metric units are selected the units will be:

Meter (m) for depth, liter (L) for gas consumptions, liter per minute (l/min) for RMV (respiratory minute volume), meter per minute (m/min)for ascent and descent rate.

If imperial units are selected the units will be:

Feet (ft) for depth, cubic feet (CUFT) for gas consumptions, cubic feet per minute (CUFT/MIN) for RMV (respiratory minute volume), feet per minute (ft/min)for ascent and descent rate.

The other units are the same for both metric and imperial units. The default setting for Units is Imperial.

# • Altitude of the dive (2)

The altitude of the dive should be selected by the user in Settings Menu from Sea Level to Level 7. The altitude ranges for each level are shown in the menu. The default setting for altitude is Sea level.

{1}------------------------------------------------

• Last deco stop depth (if any) (3) The last deco depth can be set to 10 ft (3 m) or 20 ft (6 m). The default setting for Last Deco Depth is 20 ft (6 m).

## • Closed Circuit settings (Set Point (SP) and Auto Switch) (4)

- o Set Point: Two SPs (High and Low) can be set from 0.4 to 1.3 atm. The default settings are 1.3 atm for Auto Switch Low-to-High SPHigh SP and 0.7 atm for Auto Switch High-to-LowSPLow SP.
- o Auto Switch: The Pelagic Dive Planner allow user to set depths for automatic switching from Low SP to High SP and automatic switching from High SP to Low SP.

At the depth set for Auto Switch Low-to High, if current set point is Low Set Point, SP will be switched automatically to High SP.

At the depth set for Auto Switch High-to-Low, if current set point is High Set Point, SP will be switched automatically to Low SP.

Auto Switch Low-to-High can be set ON or OFF, and the depth can be set from 51 ft to 200 ft (15 m to 60 m). Deafult setting is OFF (100 ft/30 m if set to ON)

Auto Switch High-to-Low can be set ON or OFF, and the depth can be set from 10 ft to 50 ft (3 m to 15 m). Default setting is OFF (50 ft/15 m if set to ON).

The auto switching Low-to-High and High-to-Low can occur only once during dive.

Note: In general, a Closed Circuit (CC) dive begins with Low SP. At bottom, the diver uses High SP. During ascent, at shallow depths the diver switches back to Low SP. Auto Switch SP feature enables the user to easily plan such a dive without having to enter new segments to switch SP manually.

- Extended mix Mix switch Switch stopStop: adding stop time when switching gases before the first deco stop (if any deco stop). (5) Setting extended mix switch stop ON will add a stop before a mix switch if the switch occurs before the first deco stop (if there is deco stop(s)). The time for extended mix switch stop can be set 1, 2, 3, 4 or 5 min. The default setting is OFF.
- Gradient Factors (GFs) (6) The gradient factors (High and Low) can be set from 15 to 95. The default values are 85 for GF High and 30 for GF Low.
- Equivalent Narcotic Deph (END) and Work of Breathing (WOB) Alarms (7)

Pelagic Dive Planner User Guide v0.5.07 18.12.2015 Page 2

**Biçimlendirdi:** Yazı tipi: Kalın

{2}------------------------------------------------

The END is the depth where the narcotic effect of AIR would be equal to the narcotic effect of the gas at breathed at current depth.

If O2 Narcotic option is selected, oxygen in the breathing gas will be considered as a narcotic gas similar to nitrogen.

The Planner will warn you about END when END reaches to END Alarm depth set.

The END Alarm can be set from 100 ft to 200 ft (30 m to 60 m). Defaılt setting is OFF (100 ft/30 m if set to ON).

The WOB is the depth where the Work of Breathing of AIR would be equal to the Work of Breathing of the gas at breathed at current depth.

The Planner will warn you about WOB when WOB reaches to WOB Alarm depth set.

The WOB Alarm can be set from 100 ft to 200 ft (30 m to 60 m). Defaılt setting is OFF (100 ft/30 m if set to ON).

#### • Respiratory Minute Volume (RMV) for gas consumption estimates (8)

The RMV is the Air comption per minute of breathing by the diver at surface. This parameter is used to calculate gas comsumptions during a dive.

Two RMVs can be set in Dive Setting Menu from 0.50 30 cuft to 1.504.00 cuft (8.4 5 l/min to 113.2 3 l/min). RMV Dive is the RMV during dive and RMV Deco is used during deco stops.

The default setting for RMV Dive is 0.85 90 cuft (24 25 l/min) and for RMV Deco is 0.65 70 cuft/min (18 20 l/min).

Click Save to save your dive settings and return to the main frame. Click Default, and then Save to reset the Dive Settings to default values.

Note that all dive setting can be set during dive planning and if any setting is altered, the dive plan will be automatically updated according to new settings.

Note: Switching units from imperial to metric then metric to imperial again or vice versa may cause changes in set points due to rounding of values during unit conversions. Please check your setting after switching the units.

{3}------------------------------------------------

![](_page_3_Figure_0.jpeg)

{4}------------------------------------------------

## <span id="page-4-0"></span>**3.2. Gas Settings and Selection**

A maximum of 6 gases can be set for a dive plan.

| GAS NO |                                                        |             |      |                 |        |                                          |   |    | GAS NAME FO2   FHe  PO2 Max   MOD   HT   END   WOB  CU FT   %RSRV  OC CC OFF |
|--------|--------------------------------------------------------|-------------|------|-----------------|--------|------------------------------------------|---|----|------------------------------------------------------------------------------|
| 01     | HX 7   07 = 93 = 1.6 =                                 |             |      |                 | 721 ft | 66 ft    999 ft    999 ft                | 0 | 10 | 0<br>O<br>ﮯ                                                                  |
| 05     | TX 30/25                                               |             |      | 30 - 25 - 1.4 - |        | 121 ft     0 ft     376 ft   256 ft      | 0 | 10 | 0<br>C<br>O                                                                  |
| 03     | NX 50                                                  | 50 = 00 =   |      | 1.6 =           |        | 72ft    0ft   335ft  190ft               | 0 | 10 | ()<br>O                                                                      |
| 04     | NX 50                                                  | 50 =   00 = |      |                 |        | 1.6 = 72 ft     0 ft   335 ft   190 ft   | 0 | 10 | (0)<br>( )<br>1                                                              |
| 05     | NX 80                                                  | 80 =        | 00 → |                 |        | 1.6 = 33 ft     0 ft     887 ft   182 ft | 0 | 10 | 9<br>( )                                                                     |
| 06     | OXYGEN 100 - 00 - 1.6 - 19 ft   0 ft   999 ft   176 ft |             |      |                 |        |                                          | 0 | 10 | 000                                                                          |

Figure 2. Gas List

• Start with selecting the gas you want to use for the dive plan, by setting their states to CC (Closed Circuit) or OC (Open Circuit), and leaving the other gases to OFF.

If CC is selected, the gas is a diluent for Closed Circuit Rebreather. If a dive begins with a CC gas, it is a closed circuit dive and only CC gases will be used for this dive.

{5}------------------------------------------------

If OC is selected, the gas is Open Circuit gas. If a dive begins with a OC gas, it is a open circuit dive and only CC OC gases will be used for this dive.

If OFF is selected, the gas will not be used.

Set the gas composition by setting FO2 (fraction of oxygen in percentage) and FHe (fraction of helium in percentage)for each gas you will use in the dive plan. If FO2 = 21 and FHe = 0 the gas will be named as AIR. If FO2 > 21 and FHe = 0, the gas will be named as NX (FO2). For example if FO2 = 32 and FHe = 0, the gas will be named as NX 32 (nitrox 32).

If FHe > 0, the gas will be named as TX (FO2/FHe). For example if FO2 = 18 and FHe = 45, the gas will be named as TX 18/25 (trimix 18/45)

If FO2 + FHe = 100, the gas will be named as HX (FO2/FHe). For example if FO2 = 30 and FHe = 70, the gas will be named as HX 30/70 (heliox 30/70).

If FO2 = 100, the gas will be named as OXYGEN.

The FO2 can be set from 7% to 100% and teh FHe can be set from 0% to 93%. The sum of FO2 and FHe cannot be greater than 100%. If FO2 + FHe is currently 100%, you cannot increase FO2 and FHe. In that case, you have to decrease one of them to increase the other.

Note: The gas used at the begining of a dive can not be edited.

• Select the maximum PO<sup>2</sup> to be allowed for each gasOC gases. PO2 Max setting is not available for CC gases.

During a CC dive, after deco entry, the planner will switch automatically to a CC gas with higher FO2, with PO<sup>2</sup> at current depth not exceeding PO<sup>2</sup> 1.05 atmMAX set for the user.

During a OC dive, after deco entry, the planner will switch automatically to a OC gas with higher FO2, with PO2 at current depth not exceeding PO<sup>2</sup> MAX set for the user.

Automatic gas switching will not occur before deco entry or if no deco.

Finally, if you already know the capacity of your tanks, enter their capacity and reserve percentages. The capacity of a tank is the total volume of gas in 1 atm, compressed into the tank.

If the gas comsumption exceeds this capacity during the dive, the planner will not allow you to perform such a dive.

The planner will warn you at the point where the volume of gas decreases to the reserve.

In the gas list, each gas has a:

- Color and Number
- MOD: Maximum Operating Depth, the depth where PO2 is equal to PO2 MAX (only if OC gas)

{6}------------------------------------------------

- HT: Hypoxic Threshold, the depth where PO2 = 0.21 ATA, only for TX)
- END: The depth where the equivalent narcotic depth is equal to END Alarm (only if OC gas, blank if END alarm is set to OFF)
- WOB: The depth where the work of breahting is equal to WOB Alarm) (only if OC gas, blank if WOB alarm is set to OFF)

## <span id="page-6-0"></span>**4.3. Adding Segments**

A segment is a part of a dive, define by a Depth and Duration. The segment includes the descent or ascent to that Depth, and the ascent/descent time.

For example, in order to dive at 20 meters for 15 minutes, you must add a segment of 20 meters and 15 minutes.

Click Segments -> Add in the Segments List.

Select your target depth and the segment duration. In our example, 20 meters and 15 minutes.

Select the gas to be used for the segment.

Click Add.

Upon adding the new segment, the dive plan recalculates and the dive graph is generated.

{7}------------------------------------------------

|          |                  |       |      |                  |       |     |             |      |       |    |                  | Pelagic Dive Planner                                                                                                                                                           |        |          |           |       |                    |          |
|----------|------------------|-------|------|------------------|-------|-----|-------------|------|-------|----|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|----------|-----------|-------|--------------------|----------|
| File     | Print<br>Dive    | Help  |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
| Gas List |                  |       |      |                  |       |     |             |      |       |    |                  | Dives                                                                                                                                                                          |        |          |           |       |                    |          |
|          | GAS NO GAS NAME  | FO@   |      | FHo POc Max      | MOD   | HT  | END         | мов  |       |    | % RSRV OC CC OFF | DIVE 1                                                                                                                                                                         | +      |          |           |       |                    |          |
| 01       | AR               | 21 0  | 00 - | 1.6 5            | 66 m  |     | 0 m    60 m | 60 m | o     | 10 | 000              |                                                                                                                                                                                |        |          |           |       |                    |          |
| 02       | TX 30/26         | 30 -  |      | 28 - 1.4 -       | 36 m  |     | 0 m   114 m | 78 m | o     | 10 | 000              | Segments                                                                                                                                                                       |        |          |           |       |                    |          |
| 03       | NX 50            | 50 =  |      | 00 = 1.6 == 21 m |       |     | 0 m   102 m | 58 m | 0     | 10 | 000              | Add Edit                                                                                                                                                                       | Delete |          |           |       |                    |          |
| 04       | NX 50            | 50 ÷  | 00 ÷ | 1.6 --           | 21 m  | 0 m | 102 m       | 58 m | 0     | 10 | 000              | SEGMENT                                                                                                                                                                        | DEPTH  | RUN TIME | GAS NO    |       | GAS NAME DESC Rate | ASC Rate |
| 05       | NX 80            | 80 ÷  |      | 00 + 1.6 -       | 10 m  |     | 0 m 270 m   | 55 m | 0     | 10 | 000              | SEG. 1 ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | 20 m   | 15 min   | 01 01     | AIR   | 18 m/min           | 9 m/min  |
| 90       | OXYGEN           | 100 = |      | 00 + 1.6 -       | 5 m   | 0 m | 304 m       | 53 m | 0     |    | 10 000           |                                                                                                                                                                                |        |          |           |       |                    |          |
|          | Dive Information |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
| Dive     | Plan             |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
| 0 m      |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           | DEPTH | DIVE TIME          |          |
| 22 m     |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           | 20 M  |                    | 00:11:00 |
| G.7 m    |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       | GAS 1 CONSUMP.     | GTR      |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | AIR       |       | - L                | - MIN    |
| 10 m     |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | MAX DEPTH |       | CEILING            | TTS      |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | 20 M      |       | 0 M                | 3 MIN    |
| 13 m     |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | PO2 B     |       | O2 SAT             | GF       |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
| 17 m     |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | 0.63 ATM  |       | 5 %                | -21 %    |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | END       |       | EAD                | WOB      |
| 20 m     |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          | 20 M      |       | 20 M               | 20 M     |
|          |                  |       |      |                  |       |     |             |      |       |    |                  |                                                                                                                                                                                |        |          |           |       |                    |          |
|          |                  | 02:27 |      |                  | 04:55 |     | 07:23       |      | 09:50 |    | 12:10            |                                                                                                                                                                                | 14:46  | 17:14    |           |       |                    |          |

Figure 4. Dive Plan of 20m for 15min

You can add further segments for different levels. For example, after these 15 minutes at 20 meters, you could dive to another 10 minutes at 30m. To do this, add a segment of 10 minutes and 30 meters.

You can set the descent (or ascent) rate for and set the SP (if the gas is a CC gas) for the segment in Add New Segment Dialog window.

You can see futher information by opening "Advanced" information clicking the arrow at bottom left. Advanced information includes PO2 range (max and min PO2), maximum END and WOB (only if OC gas), O2 Sat, Ceiling and Gas Comsumption calculated for this segment.

Note: You can edit the LAST segment by double clicking on segment's line in SEGMENTS section or by clicking on edit button at top of SEGMENTS section.

Note: The time of descent or ascent to a segment's depth is included in segment's time. As the result, the time of segment cannot be shorter than the descent or ascent time to the segment's depth. If the time set is shorther than descent or ascent time, the planner will automaticaaly set the time equal to descent or ascent time.

Note: If the next segment's gas is different than the previous segment's gas, the gas switch will occur AFTER the descent of ascent to the next segment's depth.

Note: If you want to change gas manually during descent, you can add a zero time segment with the new gas at the depth you want to switch to new gas then add new segment(s) with the new gas.

Note: If any deco stop is longer than 200 minutes, the dive cannot be planned. In this case you will receive the message "Deco stop is too long. Please consider your dive depth/duration and/or using another deco gas".

Pelagic Dive Planner User Guide v0.5.07 18.12.2015 Page 8

{8}------------------------------------------------

|                                                                                          |              | Biçimlendirdi: Yazı tipi: Kalın |
|------------------------------------------------------------------------------------------|--------------|---------------------------------|
| Note: Repetitive dives can be added using Add Dive button                                | next to DİVE | Biçimlendirdi: Yazı tipi: Kalın |
| # at top. Do not forget to enter the surface interval between the dives. You cannot edit |              | Biçimlendirdi: Yazı tipi: Kalın |
| the previous dive after adding a repetitive dive.                                        |              |                                 |
|                                                                                          |              | Biçimlendirdi: Yazı tipi: Kalın |

{9}------------------------------------------------

## <span id="page-9-0"></span>**5.4. Reading the Dive Plan**

Once all your Dive Settings, Gas List and Segments are entered, you can read your dive plan. Several outputs are available:

• Dive: displays the dive profile. Each gas has its own color. Icons show warnings (END, WOB alarms, O<sup>2</sup> Saturation and gas capacity levels) and Gas changes. You can move the mouse pointer over the graph to read more details on the dive watch.

Along with the graph is displayed the Dive watch: the dive watch show all the parameters as they would be displayed on your dive watch at a given time during the dive. You can move your to update the display of the dive watch.

- Plan: The Plan has three panes:
	- o Detailed Plan on the left pane details every segment of the dive. Every ascent, descent, constant depth segment, stop, and gas switch is given with detailed dive parameters. A sum-up of gas consumption is given to help you ESTIMATE the quantity of gas needed during the dive.
	- o Summary on the center shows only the Depths, the Runtimes and the Gases of the segments and deco stops.
	- o Lost Gas on the right pane when using several gases, shows the deco stops you will need to follow in case you lose one of your gases during the decompression.

In the following figures, a dive on AIR of 15min at 20m followed by 25min at 60m was done. It is a dive with decompression stops.

| 次           |                          |                 |       |             |      |       |    |       |   |                  | Pelagic Dive Planner |          |        |          |          |          |           |           |           |          |
|-------------|--------------------------|-----------------|-------|-------------|------|-------|----|-------|---|------------------|----------------------|----------|--------|----------|----------|----------|-----------|-----------|-----------|----------|
| File        | Dive Print Help          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          |           |           |           |          |
| Gas List    |                          |                 |       |             |      |       |    |       |   |                  | Dives                |          |        |          |          |          |           |           |           |          |
|             | GAS NO GAS NAME          | FO2             |       | FHe POs Max | MOO  |       |    |       |   | % RSRV OC CC OFF | DIVE 1               |          | +      |          |          |          |           |           |           |          |
| 01          | AR                       | 21 =            |       | 00 \$ 16 \$ | 66 m | 0 m   |    |       | 0 | 10 @ OC          |                      |          |        |          |          |          |           |           |           |          |
| 02          | TX 15/40                 | 15 =            |       | 40 : 1.4 :  | 83 m | 4 m   |    |       | 0 | 10 000           | Segments             |          |        |          |          |          |           |           |           |          |
| 03          | NX 50                    | 50 - 00 - 1.6 - |       |             | 21 m | 0 m   |    |       | o | 10 000           | Add   Edit           |          | Delete |          |          |          |           |           |           |          |
| 04          | NX 50                    | 50 -            |       | 00 - 1.6 -  | 21 m | 0 m   |    |       | 0 | 000<br>10        | SEGMENT              |          | DEPTH  |          | RUN TIME | GAS NO   | GAS NAME  | DESC Rato |           | ASC Rate |
| 05          | NX 80                    | 80 = 00 = 1.6 = |       |             | 10 m | 0 m   |    |       | 0 | 000<br>10        | SEG. 1               |          | 20 m   | 15 min   |          | 01       | AR        | 18 m/min  |           | 9 m/min  |
| 08          | OXYGEN                   | 100 수           |       | 00 + 1.6 =  | 5 m  | 0 m   |    |       | 0 | 10 000           | SEG. 2               |          | 60 m   | 25 min   |          | 8        | TX 15/40  | 18 m/min  |           | 9 m/min  |
| Dive<br>0 m | Dive Information<br>Plan |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          |           |           |           |          |
| 10 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        | A        |          |          | DEPTH     |           | DIVE TIME |          |
|             |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | 60 M      |           | 00:38:45  |          |
| 20 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | GAS2      | CONSUMP.  |           | GTR      |
|             |                          |                 |       |             |      |       | Fi |       |   |                  |                      |          |        |          |          |          | TX 15/40  | - L       |           | - MIN    |
| 20 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          |           |           |           |          |
|             |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | MAX DEPTH | CEILING   |           | TTS      |
| 40 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | 60 M      | 36 M      |           | 78 MIN   |
|             |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | PO2       | O2 SAT    |           | GF       |
|             |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | 1.04 ATM  | 32 %      |           | -11 %    |
| 50 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          |           |           |           |          |
|             |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | END       | EAD       |           | WOB      |
| 60 m        |                          |                 |       |             |      |       |    |       |   |                  |                      |          |        |          |          |          | 30 M      | 30 M      |           | 36 M     |
|             |                          |                 | 17:40 |             |      | 35:20 |    | 52.00 |   | 01:10:40         |                      | 01:20:20 |        | 01:46:00 |          | 02:03:40 |           |           |           |          |

Figure 6. Dive Graph with warning and gas switch icons

{10}------------------------------------------------

| 200                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                    |                                                                              |                                     |                                              |           |                                                                                 |                                                                                                                       |                      | Pelagic Dive Planner                                                                                          |                                 |                                                                                                                                                |                                                                                                                                                                                                                                          |                                                                                                                                  |                                                                                                                        |                |                       | 0<br>-              |   |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|-------------------------------------|----------------------------------------------|-----------|---------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|----------------------|---------------------------------------------------------------------------------------------------------------|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|----------------|-----------------------|---------------------|---|
| File<br>Dive<br>Print<br>Gas Ust<br>GAS NO GAS NAME<br>01<br>AIR<br>02<br>TX 15/40<br>03<br>NX 50<br>04<br>NX 50<br>05<br>NX 80<br>06<br>CONYGEN                                                                                                                                                                                                                                                                                                                                                                          | Help<br>FO2<br>21 :<br>15 =<br>40 * 1.4 *<br>50 --<br>00 + 1.6 +<br>50 --<br>80 -<br>100 -<br>00 - | FHe POz Max   MOD  <br>00 - 1.6 - 66 m<br>00 - 1.6 -<br>00 = 1.6 =<br>1.6 -- | 83 m<br>21 m<br>21 m<br>10 m<br>5 m | HT<br>0 m<br>4 m<br>0 m<br>0 m<br>0 m<br>0 m | END   WOB | 0<br>0<br>0<br>0<br>0<br>0                                                      | 1 % RSRV OC CC OFF<br>10 @ 00<br>10 0<br>10 0<br>10 0<br>10 000<br>10 0                                               | 00<br>00<br>00<br>00 | Dives<br>DIVE 1<br>Segments<br>Add   Edit<br>SEGMENT<br>SEG 1                                                 | +<br>Delete<br>DEPTH<br>20 m    |                                                                                                                                                | RUN TIME<br>15 min                                                                                                                                                                                                                       |                                                                                                                                  | GAS NO<br>01                                                                                                           | GAS NAME<br>AR | DESC Rato<br>18 m/min | ASC Rate<br>9 m/min |   |
| Dive Information<br>Plan<br>Dive<br>GF: 30% / 85%, Last Stop: 3 M, 02 Narootic: NO<br>END AL: OFF, WOB AL: OFF<br>Dive/Deco RMV: 25.5/20.0 L<br>Altitude: SEA, Extended Mix Switch Stop: OFF,<br>High SP: 1.30, Low SP: 0.70,<br>Auto Switch High: OFF, Auto Switch Low: OFF,<br>TOTAL RUNTIME: 02:03:40<br>TOTAL ASCENT TIME: 01:23:40<br>O2 SAT: 1074<br>Gas Consumption:<br>GAS 1 - AIR (OC): 1919 L<br>GAS 2 - TX 15/40 (OC): 4328 L<br>GAS 3 - NX 50 (0C): 2490 L<br>to 20 M - for 1.1 min<br>at 20 M - for 13,9 min | AIR (OC) - RUNTIME: 1.1 min<br>PO2: 0.63, END: 20 M. WOB: 20 M<br>AIR (OC) - RUNTIME: 15 min       |                                                                              |                                     |                                              | A         | SUMMARY<br>20<br>ല്ലാ<br>30<br>27<br>24<br>21<br>18<br>15<br>12<br>ತಿ<br>6<br>3 | Depth (m) Runtime (min) Gas<br>15<br>40<br>45,3<br>46.7<br>49<br>51.3<br>53.7<br>રુક<br>64.3<br>72.7<br>જુને<br>123 3 |                      | SEG. 2<br>AIR<br>TX 15/40<br>AIR<br>AIR<br>AIR<br>NX 50<br>NX 50<br>NX 50<br>NX 50<br>NX 50<br>NX 50<br>NX 50 | 60 m<br>35<br>33<br>ு<br>e<br>3 | OK<br>20 15(15)<br>60 25(40)<br>30 2(45.3)<br>27 1(46.7)<br>24 2(49)<br>21 2(51.3)<br>18 2/53.7<br>15 4(58)<br>12 6(64.3)<br>8(72.7)<br>16(89) | 25 min<br>Dive with unusable deco gas scenarios<br>AIR<br>15(15)<br>25(40)<br>1(43.7)<br>1(45)<br>3(48.3)<br>2(50.7)<br>4(55)<br>3(58.3)<br>2000.71<br>4(65)<br>7(72.3)<br>9(81.7)<br>19(101)<br>34(123.3) 39(140.3) 34(123.3) 72(183.3) | TX 15/40 NX 50<br>15(15)<br>25(40)<br>2(45.3)<br>1(46.7)<br>2(49)<br>2(51.3)<br>2(53.7)<br>4(58)<br>6/64 3)<br>8(72-7)<br>16(89) | 02<br>15(15)<br>25(40)<br>2(45.3)<br>1(46.7)<br>2(49)<br>2(51.3)<br>3/54.7)<br>6(61)<br>8(E9 3)<br>15(84.7)<br>26(111) | TX 15/40       | 18 m/min              | 9 m/min             | A |

 Figure 7. Plan for given dive and Lost Gas scenario

## <span id="page-10-0"></span>**8.5. Printing Dive Information**

It is possible to print the dive plan. Four options are offered:

- Detailed Plan: Reproduces the content of the detailed dive plan as given in Figure 7.
- Summary: Reproduces only the depth, runtime and gas information of the dive as in the Figure 7.
- Lost Gas Scenario: Reproduces the information about gases consumed during the dive as shown in the Figure 7.
- All: This option enables you to print every document stated above.

In order to print down the dive information, click Print from top menu as in the Figure 8.

{11}------------------------------------------------

| 0<br>23               |                          |      |     |  |                |   |                     |  |  |  |  |  |  |
|-----------------------|--------------------------|------|-----|--|----------------|---|---------------------|--|--|--|--|--|--|
| File<br>Dive<br>Print | Help                     |      |     |  |                |   |                     |  |  |  |  |  |  |
| Gas List              | Detailed Plan            |      |     |  |                |   |                     |  |  |  |  |  |  |
| GAS NO<br>GAS N       | Summary                  | MOD  |     |  | HT   END   WOB | - | OC CC OFF<br>% RSRV |  |  |  |  |  |  |
| 01<br>AIR             | Lost Gas Scenario        | 66 m | 0 m |  |                | 0 | 10                  |  |  |  |  |  |  |
| 02<br>TX 15           | AII                      | 83 m | 4 m |  |                | 0 | ●<br>10             |  |  |  |  |  |  |
| 03<br>NX 50           | 20 -<br>00 - 1.6 - 21 m  |      | 0 m |  |                | 0 | 10                  |  |  |  |  |  |  |
| 04<br>NX 50           | 50 ÷<br>00 -<br>1.6 --   | 21 m | 0 m |  |                | 0 | 10                  |  |  |  |  |  |  |
| 05<br>NX 80           | 80 ---<br>00 -<br>1.6 =  | 10 m | 0 m |  |                | 0 | 10                  |  |  |  |  |  |  |
| 06<br>OXYGEN          | 100 --<br>1.6 --<br>00 - | 5 m  | 0 m |  |                | 0 | 10                  |  |  |  |  |  |  |

Figure 8. Print Option

### <span id="page-11-0"></span>**11.6. Exporting Dive Information as a PDF File**

In order to export the dive information, choose File -> Export -> To PDF from the top menu as in the Figure 9. The options (Detailed Plan, Summary, Lost Gas Scenario, All) serve the same purpose as in the Printing Function.

To be able to export the dive information as a PDF file, a PDF Printer must be installed on the computer.

| C<br>23                     |               |                                             |                   |                   |  |   |                     |
|-----------------------------|---------------|---------------------------------------------|-------------------|-------------------|--|---|---------------------|
| File<br>Dive<br>Print       | Help          |                                             |                   |                   |  |   |                     |
| New                         |               |                                             |                   |                   |  |   |                     |
| Load                        | FHe           | PO2 Max   MOD   HT   END   WOB   L<br>1.6 - |                   |                   |  |   | % ASRV<br>OC CC OFF |
| Save                        | 00 =          |                                             | 66 m     0 m    + |                   |  | 0 | 10                  |
| Save As                     | 40 =<br>A     | 1.4 =                                       |                   | 83 m   4 m    +   |  | 0 | 10                  |
| Export                      | To PDF        |                                             |                   | Detailed Plan     |  | 0 | 10                  |
| Exit                        | B<br>>        | -10 -                                       |                   | Summary           |  | 0 | 10                  |
| NX 80    80    80   <br>105 | 00 =          | 1.6 --                                      |                   | Lost Gas Scenario |  | 0 | 10                  |
| 06<br>OXYGEN                | 00 ÷<br>100 = | 1.6<br>+                                    | AII               |                   |  | ם | 10                  |

Figure 9. Export as PDF Option

**Biçimlendirilmiş:** Resim Yazısı, Ortadan