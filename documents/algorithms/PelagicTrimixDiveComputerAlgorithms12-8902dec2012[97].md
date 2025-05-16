# Pelagic Trimix Dive Computer Algorithms ©  

by John E. Lewis, Ph.D. and Murat Egi, Ph.D.  

1 December  2012  

Pelagic Doc. No. 12-8902  

# Introduction  

The purpose of this document is to define the algorithms and calculations that are necessary and sufficient for the programming of a Hollis trimix dive computer.  The fundamental algorithm is based on the Buhlmann ZHL-16c algorithm that can be found in Tauchmedezin (2002) pg 158.  

The calculations require the following input data, which is assumed to be available.  

1.  Ambient pressure when the dive computer is first turned on   
2.  Multiple Tank capacities (cubic feet and pressure) and mix as defined by percentage   
of oxygen and helium (see User Input Values)   
3.  Set points for Closed Circuit Rebreathers (user input values)   
4.  Depth at 1 second intervals   
5.  Average tank pressure at 10 second intervals  

The document is divided into 17 sections and 2 appendices that contain the following information.  

# Parameter Definition (pg 5-7)  

General description of all parameters used in the algorithm and their units.  

# Fixed Parameter Values (pg 8)  

Parameter values that must be stored and accessible for calculations consisting of the Buhlmann values.  

# User Input Values (pg 9)  

Parameter values that must be stored and accessible for calculations consisting of the user input values.  

# Tension Calculations (pg10-11)  

Formulas for updating the nitrogen and helium tensions every 1 second.  

# No Decompression Times (pg 12)  

The formulas for calculating no decompression times.  

# Introduction (cond)  

# Decompression Stops and Times (pg 13-14)  

The formulas for calculating decompression stops and times.  

# Repetitive Diving (pg 15)  

The formula modifications for repetitive diving.  

# Desaturation Time (pg 15)  

The formulas for calculating de-saturation times.  

# Gradient Factor (pg 15-16)  

The formulas for calculating the actual gradient factor that is dimensionless.  

# Altitude (pg 17)  

The formulas for calculating parameters appropriate for diving at altitude.  

# Ascent Rate (pg 17)  

The formula for calculating the ascent rate during the dive in units of ft/min.  

# Gas Time Remaining (pg 18)  

The formula for calculating gas time remaining during the dive in units of min.  

# Oxygen Toxicity (pg 19-20)  

The formulas for calculating pulmonary oxygen toxicity limits.  

# Pre-Dive Planning Requirements (pg 20-21)  

The general description of the predive planning requirements.  

# PreDive Planning equations (pg 21-24)  

The formulas for pre-dive planning.  

# Introduction (cond)  

# Isobaric Counter Diffusion (pg 24)  

The formulas for calculating allowable gas mix switches.  

# Hollis Algorithm Implementation Validation (pg 25-28)  

Dive examples for testing.  

# CCR Equations (pg 29-34)  

Equations that govern Closed Circuit Rebreathers.  

Tissue Loading Bar Graph (pg  34-37)  

Equations that govern the TLBG.  

# Gradient Factor Bar Graph (pg 38-39 )  

Equations that govern the GFBG.  

Appendix A.  (pg 40-42)  

Gas Mix Recommendation  

# Appendix B.  (pg 43-52)  

Derivation and Proof of Governing Equations  

Appendix C.  (pg 53  )  

Calculations  

# Parameter Definition  

$\mathbf{A}_{\mathrm{R}}$ is the actual measured ascent rate with units of ft/min   
$\mathbf{A}_{\mathrm{{R}}}^{\prime}$ is the assumed ascent rate used for projecting values with units of ft/min   
$\mathrm{a}_{\mathrm{i}\mathrm{N}2}$ is the ith nitrogen compartment parameter value with units of fsw   
$\mathbf{a}_{\mathrm{iHe}}$ is the ith helium compartment parameter value with units of fsw   
$\mathbf{a}_{\mathrm{i}}$ is the ith compartment parameter value with units of fsw   
$\mathbf{b}_{\mathrm{{iN}}2}$ is the ith nitrogen compartment parameter value that is dimensionless   
$\boldsymbol{\mathrm{b}}_{\mathrm{iHe}}$ is the ith helium compartment parameter value that is dimensionless   
$\mathbf{b}_{\mathrm{i}}$ is the ith compartment parameter value that is dimensionless   
Cap is the tank capacity in units of cubic feet   
D is depth in units of fsw   
DC is the maximum allowable depth or ceiling with units of fsw   
$D_{\textup{R}}^{\prime}$ is the assumed descent rate used for projecting values with units of ft/min   
Dsat is the time required to reduce all compartment tensions to less than 1 fsw   
$\mathrm{DS_{i}}$ is the deepest decompression stop the ith compartment and has units of fsw   
DS is the maximum of all $\mathrm{DS_{i}}$   
Exp ( )  is the exponential function  of the argument where $\mathrm{Exp}(1)=2.7183$   
FR is the fraction of gas pressure relative to 3000 psi   
$f_{\mathrm{N}2}$ is the fraction of nitrogen in the breathing gas   
$f_{02}$ is the fraction of oxygen in the breathing gas   
$f_{\mathrm{He}}$ is the fraction of helium in the breathing gas  

# Parameter Definition (cond)  

GF is the gradient factor is dimensionless and varies between 0 and 0.95 $\mathrm{GF_{low}}$ is the gradient factor for the deepest decompression stop, and can be as low as zero $\mathrm{{GF_{high}}}$ is the gradient factor at the surface, and can be as high as 0.95  

GF(D) is the gradient factor at the depth D that varies linearly between $\mathrm{GF_{low}}$ at the   
deepest stop and $\mathrm{{GF_{high}}}$ at the surface   
GTR is the gas time remaining in units of minutes   
Max ( ) is the maximum value of the argument   
$\mathbf{M}_{\mathrm{i}}$ is the ith compartment allowable tension and has units of fsw   
MP is the equivalent mass of the tank gas and has arbitrary units of psi   
NDL is the no decompression limit and has units of minutes   
P is the pressure in units of  fsw   
$\mathrm{\DeltaP_{amb}}$ is the ambient pressure in units of fsw   
$\mathbf{P_{ambtol}}$ is the Buhlmann allowable ambient pressure in units of fsw   
$\mathrm{\bfP_{\mathrm{init}}}$ is the ambient pressure when the unit is first turned on with units of fsw   
$\mathrm{P_{tank}}$ is the tank pressure in units of psia   
PRES is the tank reserve pressure in units of psia   
PPO2 is the partial pressure of oxygen and has units of atmospheres   
SGC is the surface gas consumption in units of psi/min   
$\uptau_{\mathrm{iN}2}$ is the nitrogen tension of the ith compartment and has units of fsw   
$\uptau_{\mathrm{iHe}}$ is the helium tension of the ith compartment and has units of fsw   
t is the time in seconds for calculations at depth and in minutes for calculations on the   
surface   
$\mathbf{t_{a}}$ is the ascent time and has units of minutes   
$\mathbf{t}_{\mathrm{d}}$ is the descent time and has units of minutes   
$\mathrm{\bft}_{\mathrm{deco}}$ is a decompression time and has units of minutes   
$\pi_{i}$ is the ith compartment total tension with units of fsw   
$\pi_{i}$ (surf) is the ith compartment total tension with units of fsw projected to the surface   
$\tau_{i N2}$ is the ith compartment nitrogen time $(\tau_{1/2}/0.693)$ with units of minutes   
$\tau_{i H e}$ is the ith compartment helium time $(\tau_{1/2}/0.693)$ with units of minutes  

# Fixed Parameter Values  

These values must be stored and accessible for calculations.  

Nitrogen Parameters   
Helium Parameters   


<html><body><table><tr><td>NnrogenT Tau 1/2</td><td>araneters Tau N2</td><td>a (fsw)</td><td>b</td><td></td><td>Tau 1/2</td><td>Tau He</td><td>araneteis a (fsw)</td><td>b</td></tr><tr><td>4.0</td><td>5.77</td><td>41.0</td><td>0.505</td><td></td><td>1.51</td><td>2.18</td><td>56.7</td><td>0.425</td></tr><tr><td>5.0</td><td>7.22</td><td>38.1</td><td>0.558</td><td></td><td>1.88</td><td>2.71</td><td>52.7</td><td>0.477</td></tr><tr><td>8.0</td><td>11.5</td><td>32.6</td><td>0.651</td><td></td><td>3.02</td><td>4.36</td><td>45.0</td><td>0.575</td></tr><tr><td>12.5</td><td>18.0</td><td>28.1</td><td>0.722</td><td></td><td>4.72</td><td>6.81</td><td>38.8</td><td>0.653</td></tr><tr><td>18.5</td><td>26.7</td><td>24.6</td><td>0.783</td><td></td><td>6.99</td><td>10.1</td><td>34.1</td><td>0.722</td></tr><tr><td>27.0</td><td>39.0</td><td>20.2</td><td>0.813</td><td></td><td>10.21</td><td>14.7</td><td>30.0</td><td>0.758</td></tr><tr><td>38.3</td><td>55.3</td><td>16.4</td><td>0.843</td><td></td><td>14.48</td><td>20.9</td><td>26.7</td><td>0.796</td></tr><tr><td>54.3</td><td>78.4</td><td>14.4</td><td>0.869</td><td></td><td>20.53</td><td>29.6</td><td>23.8</td><td>0.828</td></tr><tr><td>77.0</td><td>111</td><td>13.0</td><td>0.891</td><td></td><td>29.11</td><td>42.0</td><td>21.2</td><td>0.855</td></tr><tr><td>109.0</td><td>157</td><td>11.0</td><td>0.909</td><td></td><td>41.20</td><td>59.5</td><td>19.4</td><td>0.876</td></tr><tr><td>146.0</td><td>211</td><td>10.0</td><td>0.922</td><td></td><td>55.19</td><td>79.6</td><td>18.1</td><td>0.890</td></tr><tr><td>187.0</td><td>270</td><td>9.1</td><td>0.932</td><td></td><td>70.69</td><td>102</td><td>17.4</td><td>0.900</td></tr><tr><td>239.0</td><td>345</td><td>8.2</td><td>0.940</td><td></td><td>90.34</td><td>130</td><td>16.9</td><td>0.907</td></tr><tr><td>305.0</td><td>440</td><td>7.5</td><td>0.948</td><td></td><td>115.29</td><td>166</td><td>16.9</td><td>0.912</td></tr><tr><td>390.0</td><td>563</td><td>6.8</td><td>0.954</td><td></td><td>147.42</td><td>213</td><td>16.9</td><td>0.917</td></tr><tr><td>498.0</td><td>719</td><td>6.1</td><td>0.960</td><td></td><td>188.24</td><td>272</td><td>16.8</td><td>0.922</td></tr><tr><td>635.0</td><td>916</td><td>5.6</td><td>0.965</td><td></td><td>240.03</td><td>346</td><td>16.7</td><td>0.927</td></tr></table></body></html>  

# User Input Values  

Open circuit and backup tank capacities and breathing gas mixtures  Gas # 1 through N  

<html><body><table><tr><td rowspan="2">Gas #1</td><td colspan="3">Tank capacity (cubic feet, tank pressure, and Maximum PPO2)</td></tr><tr><td>Percent (%) oxygenPercent (%) Helium fo2(1)= %/100</td><td>fHe(1)= %/100</td><td>fn2(1) = 1- fo2 - fHe</td></tr><tr><td>Gas #2</td><td>Percent (%) oxygen fo2(1)= %/100</td><td>Tank capacity (cubic feet, tank pressure, and Maximum PPO2) Percent (%)Helium fHe(1)= %/100</td><td>fn2(1) = 1-fo2 - fHe</td></tr><tr><td></td><td></td><td></td><td></td></tr><tr><td>Gas#N</td><td>Tank capacity (cubic feet, tank pressure, and Maximum PPO2)</td><td></td><td></td></tr><tr><td></td><td></td><td></td><td></td></tr><tr><td></td><td>Percent (%) oxygen fo2(1) = %/100 fHe(1)= %/100</td><td>Percent (%)Helium fn2(1) = 1-fo2 -fHe</td><td></td></tr></table></body></html>  

Rebreather set point definition and diluent mix  

Gradient Factors GFlow GFhigh  

# Tension Calculations  

The fundamental parameters that govern all decompression calculations are the tensions of nitrogen, $\pi_{\mathrm{i}\ \mathrm{N}2}$ , and helium, $\pi_{\mathrm{i}}_{\mathrm{\scriptsize~He}}$ , for 17 compartments, the units of which are fsw.  In addition, their accompanying values of $\mathbf{a}_{\mathrm{i}}$ (fsw) and $\mathsf{b}_{\mathrm{i}}$ (dimensionless) are important elements of the calculation.  

The ambient pressure, $\mathsf{p}_{\mathrm{amb}}$ , is to be sampled when the unit is first turned on.  If the user is at sea level, i.e., $\mathrm{p_{amb}}>31$ fsw, for all 17 compartments  

$$
\pi_{\mathrm{i}\ \mathrm{N}2}=0.79\times33=26.07\qquad\pi_{\mathrm{i}\ \mathrm{He}}=0
$$  

For subsequent repetitive dives, the values are different and known.  

If the user is at altitude, i.e., $\mathsf{p}_{\mathrm{amb}}\leq31$ fsw, see the section entitled Altitude.  

At depth, D, the depth is to be sampled every second, and the user must indicate the Gas being used, e.g., Gas #1, Gas $\#2$ , etc..  The tensions of all compartments are also to be updated every second (at depth the units of t are seconds), and the governing equations are  

$\mathrm{i}=1$ to 17  

$$
\pi_{i N2}(t+1)=f_{N2}\left(D+33\right)+\left[\pi_{i N2}(t)-f_{N2}(D+33)\right]E x p\left({\frac{-1}{60\tau_{i N2}}}\right)
$$  

$$
\pi_{i H e}(t+1)=f_{H e}(D+33)+\left[\pi_{i H e}(t)-f_{H e}(D+33)\right]E x p\left({\frac{-1}{60\tau_{i H e}}}\right)
$$  

On the surface, the tensions of all compartments are to be updated every minute (on the surface the units of t are minutes), and the governing equations are  

$\mathrm{i}=1$ to 17  

$$
\pi_{i N2}(t)=0.79\times33+\left[\pi_{i N2}(0)-0.79\times33\right]E x p\left(\frac{-t}{\tau_{i N2}}\right)
$$  

$$
\pi_{i H e}(t)=\pi_{i H e}(0)\times E x p\left(\frac{-t}{\tau_{i H e}}\right)
$$  

On the surface it is assumed that the user is breathing air with $f_{02}=0.21$ and $f_{\mathrm{N}2}=0.79$ .  

At depth, it is in general necessary to project these values to the surface with the equations  

$$
{_{N2}}(s u r f a c e)=f_{i N2}~(33+{A_{R}\:}^{\prime}\tau_{i N2})+\left\vert\pi_{i N2}-f_{i N2}~(D+33+A_{_R}^{\prime}\tau_{i N2})\right\vert\mathrm{exp}~(-t_{a}/\tau_{i N2}).
$$  

$$
\dot{\mathbf{\Omega}_{i H e}}\left(s u r f a c e\right)=f_{i H e}\left(33+{A_{R}\mathbf{\Omega}^{\prime}}\tau_{i H e}\right)+\Big[\pi_{i H e}-f_{i H e}\left(D+33+{A_{R}\mathbf{\Omega}^{\prime}}\tau_{i H_{e}}\right)\Big]\mathrm{exp}\left(-t_{a}/\tau_{i H e}\right)\mathbf{\Omega},
$$  

$$
w h e r e t_{_{a}}={\frac{D}{A_{_R}^{\prime}}}
$$  

$$
\pi_{i}=\pi_{i N2}+\pi_{i H e}
$$  

$$
a_{i}=\frac{\pi_{i N2}a_{\phantom{i}i N2}+\pi_{i H e}a_{\phantom{i}i i H e}}{\pi_{i}}
$$  

$$
b_{i}=\frac{\pi_{i N2}b_{\phantom{i}i N2}+\pi_{i H e}b_{\phantom{i}i i H e}}{\pi_{i}}
$$  

Next compare these values with the allowable values $\mathbf{M}_{\mathrm{i}}$ where  

$$
M_{i}=33+G F_{h i g h}\left[a_{i}+33\left(\frac{1}{b_{i}}-1\right)\right]
$$  

If all of these surface values of $\pi_{\mathrm{i}}$ are less than $\mathbf{M}_{\mathrm{i\cdot}}$ , we need to calculate NDL using the method described in No Decompression Times.  

If any value of $\pi_{\mathrm{i}}$ is greater than $\mathbf{M}_{\mathrm{i}}$ , we need to calculate the decompression stops and times using the method described in Decompression Stops and Times.  

# No Decompression Times  

All values of $\pi_{\mathrm{i}}$ are less than $\mathbf{M}_{\mathrm{i}}$ , and our task is to find the value of NDL such that the addition of 1 minute will cause one or more of the values of $\pi_{\mathrm{i}}$ to exceed $\mathbf{M}_{\mathrm{i}}$ .  

$$
\begin{array}{c}{{\pi_{i N2}=f_{i N2}\left(D+33\right)+\left\lfloor\pi_{i N2}\left(s u r f a c e\right)-f_{i N2}\left(D+33\right)\right\rfloor\exp\left(-N D L/\tau_{i N2}\right)}}\\ {{{}}}\\ {{\pi_{i H e}=f_{i H e}\left(D+33\right)+\left\lfloor\pi_{i H e}\left(s u r f a c e\right)-f_{i H e}\left(D+33\right)\right\rfloor\exp\left(-N D L/\tau_{i H e}\right)}}\end{array}
$$  

$$
\pi_{i}=\pi_{i N2}+\pi_{i H e}
$$  

$$
a_{i}=\frac{\pi_{i N2}a_{\phantom{i}i N2}+\pi_{i H e}a_{\phantom{i}i i H e}}{\pi_{i}}
$$  

$$
b_{i}=\frac{\pi_{i N2}b_{\phantom{i}i N2}+\pi_{i H e}b_{\phantom{i}i i H e}}{\pi_{i}}
$$  

Next compare these values with the allowable values $\mathbf{M}_{\mathrm{i}}$ where  

$$
M_{i}=33+G F_{h i g h}\left[a_{i}+33\left(\frac{1}{b_{i}}-1\right)\right]
$$  

The value of NDL requires iteration.  The optimum method is expected to be the rule of halves, but the final procedure is left to the programmer.  (Note:  If the diver is at altitude refer to Section on Altitude pg 18)  

# Decompression Stops and Times  

Calculate the parameters relevant to decompression, where we must first project the values of $\pi$ to the first deepest stop, DS.  We begin by starting with compartment #1 and iterate until we find a value of $\mathrm{DS_{i}}$ that satisfies the following equations.  

$$
\pi_{i N2}=f_{i N2}~(D S_{i}+33+A_{\phantom{'}R}^{\prime}\tau_{i N2})+\left[\pi_{i N2}-f_{i N2}~(D+33+A_{\phantom{'}R}^{\prime}\tau_{i N2})\right]\exp{(-t_{i a}/\tau_{i N2})}
$$  

$$
\pi_{i H e}=f_{i H e}(D S_{i}+33+A^{\prime}{_{R}}\tau_{i H e})+\left[\pi_{i H e}-f_{i N2}(D+33+A^{\prime}{_{R}}\tau_{i H e})\right]\exp(-t_{i a}/\tau_{i H e}) 
$$  

$$
w h e r e t_{i_{a}}=\frac{(D-D S_{i})}{A_{\textsl{R}}^{\prime}}
$$  

$$
\pi_{i}=\pi_{i N2}+\pi_{i H e}
$$  

$$
a_{i}=\frac{\pi_{i N2}a_{\phantom{i}i N2}+\pi_{i H e}a_{\phantom{i}i H e}}{\pi_{i}}
$$  

$$
b_{i}=\frac{\pi_{i N2}b_{\phantom{i}i N2}+\pi_{i H e}b_{\phantom{i}i H e}}{\pi_{i}}
$$  

$$
D S_{i}=\frac{\pi_{i}-33-G F_{l o w}\left[a_{i}+33\left(\frac{1}{b_{i}}-1\right)\right]}{\left[1+G F_{l o w}\left(\frac{1}{b_{i}}-1\right)\right]}
$$  

Continue with following compartments $\#2$ , #3, etc. until the value of subsequent $\mathrm{DS_{i}}$ decrease using each of the previous values of $\mathrm{DS_{i}}$ to begin the search.  Thus, the maximum value of has been determined, but we choose to use only integer 10 ft values using the formula  

$$
\mathrm{DS}=10\mathrm{~x~Int}(1{+}\mathrm{Max}(\mathrm{DS}_{\mathrm{i}})/10)
$$  

Once DS has been defined, the value of GF, which is a function of depth, is governed by the equation  

$$
G F(D)=G F_{l o w}+\Big(G F_{h i g h}-G F_{l o w}\Big)\Bigg(\frac{D S-D}{D S}\Bigg)
$$  

Having established the depth of the first deep stop, we proceed to calculate the decompression time, beginning with values of $\pi_{\mathrm{i}}$ following the ascent to DS.  Note that at all times the user must indicate the gas mix that he is using.  

$$
\dot{\iota_{N2}}(D S)=f_{i N2}~(D S+33+A^{\prime}_{R}~\tau_{i N2})+\big[\pi_{i N2}-f_{i N2}~(D+33+A^{\prime}_{R}~\tau_{i N2})\big]\mathrm{exp}~(-t_{a}~/~\tau_{i N2})
$$  

$$
r_{_{i H e}}(D S)=f_{_{i H e}}(D S+33+A^{\prime}{}_{R}\tau_{_{i H e}})+\big[\pi_{_{i H e}}-f_{_{i N2}}(D+33+A^{\prime}{}_{R}\tau_{_{i H e}})\big]\mathrm{exp}(-t_{a}/\tau_{_{i H e}}),
$$  

$$
w h e r e t_{a}=\frac{(D-D S)}{A_{\scriptscriptstyle R}^{\prime}}
$$  

$$
\pi_{i N2}=f_{i N2}~(D S+33)+\left[\pi_{i N2}-f_{i N2}~(D+33)\right]\exp{(-t_{d e c o}/\tau_{i N2})}
$$  

$$
\pi_{i i H e}=f_{i i H e}\ (D S+33)+\big[\pi_{i i H e}-f_{i N2}\ (D+33)\big]\mathrm{exp}\ (-t_{d e c o}/\tau_{i i H e})
$$  

$$
\pi_{i}=\pi_{i N2}+\pi_{i H e}
$$  

$$
a_{i}=\frac{\pi_{i N2}a_{\phantom{i}i N2}+\pi_{i H e}a_{\phantom{i}i i H e}}{\pi_{i}}
$$  

$$
b_{i}=\frac{\pi_{i N2}b_{\phantom{i}i N2}+\pi_{i H e}b_{\phantom{i}i H e}}{\pi_{i}}
$$  

The task is to find the value of $\mathbf{t}_{\mathrm{deco}}$ such that the reduction of 1 minute will cause any of the values of $\pi_{\mathrm{i}}$ to exceed $\mathbf{M}_{\mathrm{i}}$ where  

$$
M_{i}=(D+23)+G F(D-10)\left[a_{i}+(D+23)\left(\frac{1}{b_{i}}-1\right)\right]
$$  

where $G F(\mathrm{D}\mathrm{-}10)$ refers to the value of $G F$ at the depth D-10.  

Once the decompression at DS is completed, the diver ascends 10 feet to the next stop where the required decompression is again calculated.  Differences in the values of $\pi_{\mathrm{i}}$ during the 10 ft ascent will be neglected.  All decompression stops require iteration, which is expected to be by the rule of halves.  However, the final procedure is left to the discretion of the programmer.  

# Repetitive Diving  

On the surface, for compartments $\dot{\mathbf{i}}=1$ to 8 $\tau_{\mathrm{{uN}2}}=86.6$ and $\uptau_{\mathrm{{lHe}}}=32.7$ , with the exception for tension values of nitrogen less than the initial “clean” values of $0.79\mathrm{P_{init}}$ fsw.  For these later cases the relaxation is to be symmetric, i.e., we shall use the original halftime values.  The user is to be cautioned that this is a theoretical result with no controlled data base for validation.  

# Desaturation  

In order to calculate the desaturation time, Dsat, it is necessary to find the value for which this equation is satisfied for all compartments.  See Altitude section for altitude.  

$$
\left(\pi_{i N2}-26.07\right)E x p\left(\frac{-D s a t}{\tau_{i N2}}\right)+\pi_{i H e}~E x p\left(\frac{-D s a t}{\tau_{i H e}}\right)\leq1~f s w
$$  

This will require an iteration preferably using the rule of halves.  

# Gradient Factor  

The user inputs values of $\mathrm{GF_{low}}$ and $\mathrm{GF_{high}}$ , but we may wish to display the actual value of GF (at least when it is positive), the formulas for which are given below.  

$$
G F_{i}=\frac{\pi_{i}-(D+33)}{M_{i}-(D+33)}
$$  

$$
G F=M a x\left(G F_{i}\right)
$$  

$$
M_{i}=a_{i}+{\frac{(D+33)}{b_{i}}}
$$  

An example of the behavior of the Gradient Factor is shown below, where we see that during a decompression stop the value of GF initially is close to the allowable value and then drops to a considerably lower value before the diver ascends to his next stop.  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/c506c5e263a9c51dcccba64461316e9a9c8b40adc09949fd8e6966c415e385a7.jpg)  
200 ft/10 min with Bottom Mix 18/45 and Deco mix 30/30  

# Altitude  

The algorithm for the allowable tissue values based on the Buhlmann algorithm is  

$$
P_{a m b t o l}=\left(\pi-a\right)b
$$  

$$
\pi_{a l l o w}=a+\frac{P_{a m b}}{b}
$$  

where $\mathrm{P_{amb}=P_{i n i t}\ (f s w)+D(f s w)}$ . The dive computer will sense the local pressure when it is turned on at altitude and convert the pressure at altitude to fsw.  Note that in every equation 33 is to be replaced with $\mathbf{P}_{\mathrm{init}}$ (23 with $\mathrm{P_{init}}{-}10$ ).  

We shall assume that the diver is not acclimated at that altitude, and thus the initial tension values are  

$$
\pi_{\mathrm{i}\ \mathrm{N}2}=26.07\ \mathrm{and}\ \pi_{\mathrm{i}\ \mathrm{He}}=0
$$  

The relationship between the pressure transducer and depth will not be changed and that depth will used in all calculations.  However, the displayed depth (unless otherwise specified by the user) shall be for fresh water, and  

$\mathsf{D}(\mathsf{f w})=:\mathsf{e q\mathrm{-}P}$ at Fresh Water Depth $\mathrm{D(fw)=D(fsw)/1.03}.$ D(fsw) $\mathbf{\Sigma}=\mathbf{\Sigma}$ : eq-P at Salt Water Depth  

# Ascent Rate  

The following formula for displayed actual ascent rate, $\mathbf{A}_{\mathrm{R}}$ in units of ft/min, represents a least-mean-squares fit to $\mathbf{N}$ depths sampled every one second.  

$$
{\cal A}_{{\scriptscriptstyle R}}=\frac{360}{N(N-1)}\Bigg\{\sum_{n=1}^{N}D_{n}-\frac{2}{(N+1)}\sum_{n=1}^{N}n D_{n}\Bigg\}
$$  

For $N=7$ and for t in units of seconds  

$$
A_{R}(t)={\frac{60}{7}}\biggl\{\frac{3}{4}[D(t-6)-D(t)]+\frac{1}{2}[D(t-5)-D(t-1)]+\frac{1}{4}[D(t-4)-D(t-2)]\biggr\}
$$  

# Gas Time Remaining  

The calculation of gas time remaining (GTR) begins with 10 second average values of $\mathrm{P_{tank}}$ .  For random electronic noise as great as $\pm\nobreakspace10\mathrm{psi}$ , it is necessary to sample at $10\mathrm{Hz}$ in order to achieve $1\%$ accuracy.  These 10 second average values (using 100 data points) of tank pressure are then converted into an equivalent mass, MP, to account for real gas effects.  

Extensive calculations (see Excel file Optimum trimix gas selection) indicate that the real gas effects amount to little more 2 minutes difference than regardless of the mix or depth at the beginning of the dive, converging to no difference at the end of the dive when the pressure has dropped to the reserve of 500 psi.  

$$
{\frac{d P}{d t}}={\frac{36}{N(N-1)}}\Biggl\{\sum_{n=1}^{N}P_{n}-{\frac{2}{(N+1)}}\sum_{n=1}^{N}n P_{n}\Biggr\}
$$  

For $N=7$ an for t in units of seconds  

$$
{\frac{d P}{d t}}(t)={\frac{6}{7}}{\left\{{\frac{3}{4}}[P(t-6)-P(t)]+{\frac{1}{2}}[P(t-5)-P(t-1)]+{\frac{1}{4}}[P(t-4)-P(t-2)]\right\}}
$$  

Calculate the surface equivalent consumption rate (SGC) using the average depth, $\mathrm{\DeltaD_{avg}},$ during the 60 second period,  

$$
S G C=\frac{\displaystyle\frac{d P}{d t}}{\left(1+\frac{D_{a\nu g}}{33}\right)}
$$  

Calculate GTR using the present value of MP and D  

$$
G T R=\frac{P-P_{R E S}}{S G C\left(1+\displaystyle\frac{D}{33}\right)}
$$  

For a new unit, set $S G C=25$ psi/min (average value for 80 ft tank).  New estimates of SGC are initiated 60 seconds into the first dive and then retained.  SGC is upgraded whenever a new estimate is greater than $5\mathrm{{psi}/\mathrm{{min}}}$ (the unit is assumed to not be in use if a smaller value occurs).  GTR is calculated using the latest retained value of SGC and as the initial value of a new dive prior to accumulating the required 60 seconds of data.  

# Oxygen Toxicity  

NOAA specifies limited exposures to oxygen for a single dive and for any 24 hour period.  These limitations are based on the partial pressure of oxygen, PPO2, where  

$$
P O_{2}=F O_{2}\left[1+{\frac{D}{33}}\right]
$$  

When unit is first turned on, set OTUD (daily exposure in oxygen toxicity units) and OTUS (single dive exposure in oxygen toxicity units) equal to zero.  We calculate the rate of accumulation of daily oxygen toxicity units, ROTD as follows.  

$$
\begin{array}{l l}{{P O_{2}<0.5}}&{{R O T D=0}}\\ {{}}&{{}}\\ {{0.5\leq P O2}}&{{R O T D=-0.17+0.82~P O_{2}+0.35~P O_{2}}^{2}}}\end{array}
$$  

For single dive exposures, the rate of accumulation of oxygen toxicity units, ROTS, we use the following equations  

$$
\begin{array}{r l}{P O2\leq1.00\quad}&{R O T S=R O T D}\\ {1.00<P O2\leq1.13\quad}&{R O T S=2.5P O_{2}-1.50}\\ {1.13<P O2\geq1.50\quad}&{R O T S=4.56-7.2P O_{2}+3.84P{O_{2}}^{2}}\\ {1.50<P O_{2}\quad}&{R O T S=41.7P O_{2}-60.0}\end{array}
$$  

At depth, both OTUD and OTUS are updated every second  

$$
O T U D=O T U D+R O T D{\frac{1}{60}}
$$  

$$
O T U S=O T U S+R O T S{\frac{1}{60}}
$$  

During the surface interval, OTUS is updated every 10 minutes and allowed to relax according to the formula (90 minute halftime)  

$$
O T U S=0.9259O T U S
$$  

OTUD is not allowed to relax, but is diminished by the accumulation during any dive that did not occur in the previous 24 hour period.  

At depth, calculate oxygen time remaining (OTR) for $\mathrm{PPO}2{>}0.5$  

$$
O T R=M i n\left\{\frac{300-O T U D}{R O T D},\frac{300-O T U S}{R O T S}\right\}
$$  

On the surface, for pre-dive planning calculate the OTR using the rates that correspond to the user selected $\mathrm{FO}_{2}$ and the planning depth, DP  

$$
P O_{2}=F O_{2}\left[1+{\frac{D P}{33}}\right]
$$  

For both pre-dive planning and at depth, OTR is unlimited when $P O_{2}\leq0.5$ .  

# Pre-Dive Planning Requirements  

# Open Circuit  

First as input, the user must specify depth, desired bottom time, maximum PPO2, and bottom tank capacity.   The dive planner will return a recommended bottom mix, allowable bottom time, and the depth of the deepest stop.  If required, the planner will suggest a travel mix and tank capacity requirement.  Using the depth of the deepest stop, a decompression gas mix and tank capacity requirement will be added.  In the final plan, user mixes and tank capacities for all three phases of this dive will be evaluated and equivalent air depths for narcosis and carbon dioxide retention presented.  In addition, the conditions for avoiding isobaric counter diffusion will also be presented.  

# Closed Circuit Rebreather  

Here again, the user must specify depth, bottom time, set point(s) for PPO2. However, here side slung backup tanks, that will be used only in case of a malfunction of the rebreather shall be defined.  For depths of less than 200 ft, 2 such tanks are common, whereas for greater depths the number of such tanks could require 4 or more.  Once the depth, bottom time, and PPO2 is defined, the job of the dive planner is to define the mixes and tank capacities of the backup tanks.  

Backup tanks consist of at least one with the chosen diluent mix and the othert(s) decompression tank(s), the mix of which is dependent upon the depth of the deepest decompression stop.  Some examples may suggest the addition of a pure oxygen tank as well.  All these tanks are backup units that are used only in the case of a rebreather failure associated with electronics or CO2 scrubber.  The worst case scenario is if the rebreather failed at the end of the bottom time and as such will be used for evaluating the backup tanks.  Note that the rebreather backup tanks have much in common with the open circuit tanks.  

# At Depth Requirements  

For the open circuit diver and the rebreather diver that has to resort to the use of his backup tanks, the diver must have the capability of making the dive computer aware of the mix in use as it changes.  

# Pre-Dive Planning Equations  

User specifies that he wishes to find NDL, and he needs to input Gas mix and $\mathrm{{GF_{high}}}$ .  We need to descend, stay for a time, and ascend arriving at acceptable values of $\pi_{\mathrm{i}}$ ,i.e., all $\pi_{\mathrm{i}}<\mathbf{M}_{\mathrm{i}}$ .  This will require iteration to find the allowable bottom time.  We shall assume that $\mathrm{D}_{\mathrm{R}}^{\prime}=60$ ft/min and $\mathrm{A^{\prime}{_R}}=30$ ft/min.  

Descend and calculate for $\mathrm{i}{=}1$ to 17 values of $\pi_{\mathrm{i}}$  

$$
\pi_{i N2}=f_{N2}\left(D+33-D_{_R}^{\prime}\tau_{i N2}\right)+\left[\pi_{i N2}(0)-f_{N2}(33-D_{_R}^{\prime}\tau_{i N2})\right]E x p\left({\frac{-t_{d}}{\tau_{i N2}}}\right)
$$  

$$
\pi_{i H e}=f_{H e}\left(D+33-D_{\phantom{}R}^{\prime}\tau_{i H e}\right)+\left[\pi_{i H e}\left(0\right)-f_{H e}\left(33-D_{\phantom{}R}^{\prime}\tau_{i H e}\right)\right]E x p\left(\frac{-t_{d}}{\tau_{i H e}}\right)
$$  

where for a clean dive  

$$
\begin{array}{r l}{\pi_{i N2}(0)=.0.79x P_{_{I n i t}}}&{{}\pi_{i H e}(0)=0}\end{array}
$$  

Stay for a time TD (this is the value that will require iteration)  

$$
\pi_{i N2}=f_{_{N2}}\left(D+33\right)+\left[\pi_{i N2}-f_{_{N2}}(D+33)\right]E x p\left({\frac{-T D}{\tau_{i N2}}}\right)
$$  

$$
\pi_{i H e}=f_{H e}\left(D+33\right)+\left[\pi_{i H e}-f_{H e}(D+33)\right]E x p\left({\frac{-T D}{\tau_{i H e}}}\right)
$$  

Ascend and compare $\pi_{\mathrm{i}}$ with $\mathbf{M}_{\mathrm{i}}$  

$$
\pi_{i N2}=f_{N2}\left(33+{A^{\prime}}_{R}\tau_{i N2}\right)+\left[\pi_{i N2}-f_{N2}(D+33+A_{R}^{\prime}\tau_{i N2})\right]E x p\left(\frac{-t_{a}}{\tau_{i N2}}\right)
$$  

$$
\pi_{i H e}=f_{H e}\left(33+A_{\phantom{\prime}R}^{\prime}\tau_{i H e}\right)+\left[\pi_{i H e}-f_{H e}\left(D+33+A_{\phantom{\prime}R}^{\prime}\tau_{i H e}\right)\right]E x p\left(\frac{-t_{a}}{\tau_{i H e}}\right)
$$  

$$
t_{\scriptscriptstyle a}=\frac{D}{A_{\scriptscriptstyle R}^{\prime}}
$$  

$$
\pi_{i}=\pi_{i N2}+\pi_{i H e}
$$  

$$
a_{i}=\frac{\pi_{i N2}a_{\phantom{i N2}i N2}+\pi_{i H e}a_{\phantom{i i H e}i i H e}}{\pi_{i}}
$$  

$$
b_{i}=\frac{\pi_{i N2}b_{\phantom{i}i N2}+\pi_{i H e}b_{\phantom{i}i i H e}}{\pi_{i}}
$$  

$$
M_{i}=33+G F_{h i g h}\left[a_{i}+33\left(\frac{1}{b_{i}}-1\right)\right]
$$  

The task is to find the value of TD such that the addition of 1 minute will cause at least one of the values of $\pi_{\mathrm{i}}$ to exceed $\mathbf{M}_{\mathrm{i}}$ .  Allowable bottom time $\mathrm{NDL}=\mathrm{TD}+\mathrm{D}/\mathrm{D}_{\mathrm{~R~}}^{\prime}$ . Calculate and display all values of NDL for ${\mathrm{D}}=30$ to 190 fsw.  

# Decompression Dives  

If the user does not specify that he wishes to perform a NoD dive, he will need to specify a depth, Gas #1 bottom mix, travel mix, and decompression mixes, tank capacity in cubic feet and tank pressure, $\mathrm{GF_{low}}$ and $\mathrm{{GF_{high}}}$ , and a maximum PPO2.   Using the criteria specified in Appendix B, we will calculate the maximum oxygen percentage, the minimum helium percentage, and the maximum bottom time, the later of which is determined from the gas tank capacity using the equation  

$$
G T R=\frac{F_{_R}\left(1-\frac{\mathrm{P_{res}}}{3000}\right)C a p}{S G C\left(1+\displaystyle\frac{D}{33}\right)}
$$  

where the GTR is the gas time remaining, based on the input tank capacity, Cap, in cubic feet, the $\mathrm{F_{R}=}$ tank pressure/3000, where the tank pressure is an input.  

The HDC will display a recommended (See Appendix A), i.e.,  oxygen percentage, helium percentage, and a maximum bottom time, and ask for the user's choice of mix and bottom time, which is expected to be a standard mix close to the recommended ideal mix.  We shall then proceed to calculate the deepest stop and the decompression time at that stop using the mix specified by the user and the following equations  

Descend and calculate for $\mathrm{i}{=}1$ to 17 values of $\pi_{\mathrm{i}}$  

$$
\pi_{i N2}=f_{_{N2}}\left(D+33-D^{\prime}{}_{R}\ \tau_{i N2}\right)+\left[\pi_{i N2}\left(0\right)-f_{_{N2}}\left(33-D^{\prime}{}_{R}\ \tau_{i N2}\right)\right]E x p\left({\frac{-t_{d}}{\tau_{i N2}}}\right)
$$  

$$
\pi_{i H e}=f_{H e}\left(D+33-D^{\prime}{}_{R}\ \tau_{i H e}\right)+\left[\pi_{i H e}\left(0\right)-f_{H e}\left(33-D^{\prime}{}_{R}\ \tau_{i H e}\right)\right]E x p\left(\frac{-t_{d}}{\tau_{i H e}}\right),
$$  

where for a cleandive  

$$
\begin{array}{r l}{\pi_{i N2}(0)=.0.79\times P_{I n i t}}&{{}\pi_{i H e}(0)=0}\end{array}
$$  

$$
t_{d}=\frac{D}{D_{\phantom{}_{R}}^{\phantom{}}}
$$  

Stay for a time TD $\c=$ BT  - td  

$$
\pi_{i N2}=f_{_{N2}}\left(D+33\right)+\left[\pi_{i N2}-f_{_{N2}}(D+33)\right]E x p\left({\frac{-T D}{\tau_{i N2}}}\right)
$$  

$$
\pi_{i H e}=f_{H e}\left(D+33\right)+\left[\pi_{i H e}-f_{H e}(D+33)\right]E x p\left({\frac{-T D}{\tau_{i H e}}}\right)
$$  

Next we must calculate the first deepest stop and the decompression time at that stop using the equations in Decompression Times and Stops using user input Gas # 2, where we pause to calculate and display the recommended maximum oxygen content and the minimum helium content.  Note that it is not possible to determine if the available gas is sufficient to reach the final decompression stop at 20 fsw , where the user has specified a different gas, unless we perform all of the decompression stops in between.  

# Isobaric Counter Diffusion  

Deep dives require high fractions of helium in order to prevent narcosis and the retention of carbon dioxide caused by the work of breathing.  When we take into account the Isobaric Counter Diffusion Rule of 5, i.e., $\Delta\mathrm{N}2<\Delta\mathrm{He}/5$ , the helium content of the decompression gases are also relatively high.  

Fortunately, a simple algebraic equation allows us to calculate directly the fractions of helium and nitrogen given a depth dependent fraction of oxygen.  

$$
\begin{array}{r}{{F N2}_{\tiny B e c o}=F N2_{\tiny B o t t o m}+\frac{(F H e_{\tiny B o t t o m}-F H e)}{5}\qquad}\\ {F O2+F H e+F N2_{\tiny B o t t o m}+\frac{(F H e_{\tiny B o t t o m}-F H e)}{5}=1.0}\\ {F H e=\frac{5}{4}\Bigg(1.0-F O2-F N2_{\tiny B o t t o m}-\frac{F H e_{\tiny B o t t o m}}{5}\Bigg)\qquad}\end{array}
$$  

I have run several deep dives, and with the calculated deepest stop (GF 30/85) for these examples I have applied this formula to arrive at the deco mixes indicated below (FO2 values are calculated for a ${\mathrm{PPO}}2{=}1.4$ ).  

Actually, these nitrogen fractions are the maximum values, and smaller values will require higher helium fractions.  

<html><body><table><tr><td></td><td colspan="2">Bottom Mix</td><td></td><td></td><td>Deco Mix</td><td></td><td></td></tr><tr><td>Depth (ft)</td><td>FO2</td><td>FHe</td><td>FN2</td><td>Deepest Stop (ft)</td><td>FO2</td><td>FHe</td><td>FN2</td></tr><tr><td>300</td><td>0.10</td><td>0.70</td><td>0.20</td><td>150</td><td>0.25</td><td>0.51</td><td>0.24</td></tr><tr><td>250</td><td>0.15</td><td>0.55</td><td>0.30</td><td>110</td><td>0.32</td><td>0.33</td><td>0.34</td></tr><tr><td>200</td><td>0.18</td><td>0.45</td><td>0.37</td><td>80</td><td>0.41</td><td>0.16</td><td>0.43</td></tr><tr><td>150</td><td>0.18</td><td>0.3</td><td>0.52</td><td>60</td><td>0.50</td><td>0.00</td><td>0.50</td></tr></table></body></html>  

Unfortunately, none of these mixes are standard.  Since the FN2 is the maximum allowable, the sum of the FO2 and FHe must not be less, and it follows that 25/51 must be replaced by 12/65, 32/33 by 15/55, 41/16 by 30/30, and only 50/0 stands as is.  

# Hollis Algorithm Implementation Validation  

I have validated Gen Plan for the following examples, and we shall follow this matrix of dives to validate the final version of the microprocessor and dive computer.  

AIR   


<html><body><table><tr><td>Air</td><td>DR=60 AR=60</td></tr><tr><td>Depth</td><td>NDL</td></tr><tr><td>40</td><td>140.2</td></tr><tr><td>50</td><td>76.6</td></tr><tr><td>60</td><td>52.4</td></tr><tr><td>70</td><td>36.9</td></tr><tr><td>80</td><td>27.0</td></tr><tr><td>90</td><td>21.3</td></tr><tr><td>100</td><td>16.8</td></tr><tr><td>110</td><td>14.0</td></tr><tr><td>120</td><td>12.1</td></tr><tr><td>130</td><td>10.6</td></tr><tr><td>140</td><td>9.6</td></tr><tr><td>150</td><td>8.6</td></tr></table></body></html>  

NITROX   


<html><body><table><tr><td>Air</td><td>DR=60</td><td>AR=60</td><td>My Code</td><td></td></tr><tr><td>Depth</td><td>Bottom Time</td><td>30 ft</td><td>20 ft</td><td>10 ft</td></tr><tr><td>60</td><td>60</td><td></td><td></td><td>4.2</td></tr><tr><td></td><td>80</td><td></td><td></td><td>15.2</td></tr><tr><td></td><td></td><td></td><td></td><td></td></tr><tr><td>120</td><td>15</td><td></td><td></td><td>1.5</td></tr><tr><td></td><td>25</td><td></td><td>3</td><td>9.5</td></tr><tr><td></td><td></td><td></td><td></td><td></td></tr><tr><td>150</td><td>15</td><td></td><td>1.3</td><td>4.9</td></tr><tr><td></td><td>25</td><td>3.1</td><td>6.9</td><td>18.9</td></tr></table></body></html>  

<html><body><table><tr><td>Nitrox</td><td>DR=60</td><td>AR=60</td></tr><tr><td>Depth</td><td>FO2</td><td>My NDL</td></tr><tr><td>100</td><td>0.35</td><td>31.2</td></tr><tr><td></td><td></td><td></td></tr><tr><td>120</td><td>0.30</td><td>16.5</td></tr><tr><td></td><td></td><td></td></tr><tr><td>140</td><td>0.27</td><td>11.2</td></tr></table></body></html>  

# HOLLIS PROPRIETARY INFORMATION  

HELIOX   


<html><body><table><tr><td>Nitrox</td><td>DR=60</td><td>AR=60</td><td>My Code</td><td></td><td></td></tr><tr><td>Depth</td><td>FO2</td><td>Bottom Time</td><td>30 ft </td><td>20 ft</td><td>10 ft </td></tr><tr><td>100</td><td>0.35</td><td>40</td><td></td><td></td><td>3.7</td></tr><tr><td></td><td></td><td>50</td><td></td><td></td><td>10.8</td></tr><tr><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>120</td><td>0.30</td><td>40</td><td></td><td>5</td><td>17.1</td></tr><tr><td></td><td></td><td>50</td><td>0.5</td><td>10.0</td><td>24.6</td></tr><tr><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>140</td><td>0.27</td><td>20</td><td></td><td>0.9</td><td>6.0</td></tr><tr><td></td><td></td><td>30</td><td>0.9</td><td>6.1</td><td>16.5</td></tr></table></body></html>  

I am using the name heliox to indicate a mix with no nitrogen but variable oxygen content consistent with a PPO2 of 1.4.  Note that despite the breathing gas having no nitrogen, because of the initial nitrogen content nitrogen still plays a role.  

<html><body><table><tr><td>Depth</td><td>FO2/FHe</td><td>NDL</td></tr><tr><td>80</td><td>41/59</td><td>30.6</td></tr><tr><td></td><td></td><td></td></tr><tr><td>100</td><td>35/65</td><td>14.1</td></tr><tr><td></td><td></td><td></td></tr><tr><td>120</td><td>30/70</td><td>8.5</td></tr></table></body></html>  

<html><body><table><tr><td>AR=60</td><td>DR=60</td><td></td><td>My Code</td><td></td><td></td></tr><tr><td>Depth</td><td>FO2/FHe</td><td>Bottom time</td><td>30 ft </td><td>20 ft </td><td>10 ft</td></tr><tr><td>80</td><td>41/59</td><td>40</td><td></td><td></td><td>4</td></tr><tr><td></td><td></td><td>50</td><td></td><td></td><td>9.3</td></tr><tr><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>100</td><td>35/65</td><td>20</td><td></td><td></td><td>3.8</td></tr><tr><td></td><td></td><td>30</td><td></td><td>0.9</td><td>13.2</td></tr><tr><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>120</td><td>30/70</td><td>20</td><td></td><td>3.1</td><td>11.8</td></tr><tr><td></td><td></td><td>30</td><td>2.1</td><td>9.2</td><td>25.9</td></tr></table></body></html>  

# TRIMIX  

Actually, the Heliox example was in fact a mix of 3 gases, albeit with limited nitrogen content.  Regardless, I have chosen to test more serious dives with trimix, using a $\mathrm{PPO}2=1.4$ and an equivalent Air depth of 100 ft for both narcosis and work of breathing.  The resultant mixes are as follows:  

# HOLLIS PROPRIETARY INFORMATION  

<html><body><table><tr><td>Depth (fsw)</td><td>FO2</td><td>FHe</td><td>FN2</td></tr><tr><td>150</td><td>0.25</td><td>0.35</td><td>0.40</td></tr><tr><td>200</td><td>0.20</td><td>0.50</td><td>0.30</td></tr><tr><td>250</td><td>0.15</td><td>0.65</td><td>0.20</td></tr><tr><td>300</td><td>0.10</td><td>0.70</td><td>0.20</td></tr></table></body></html>  

OXYGEN   


<html><body><table><tr><td>AR=60</td><td>DR=60</td><td></td><td>My Code</td><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>Depth</td><td>FO2/FHe</td><td>Bottom time</td><td>70 ft</td><td>60 ft</td><td>50 ft</td><td>40 ft</td><td>30 ft</td><td>20 ft</td><td>10 ft</td></tr><tr><td>150</td><td>25/35</td><td>20</td><td></td><td></td><td></td><td></td><td>0.5</td><td>4.3</td><td>12.2</td></tr><tr><td>200</td><td>20/50</td><td>15</td><td></td><td></td><td></td><td>2.3</td><td>3.6</td><td>8</td><td>21.6</td></tr><tr><td>250</td><td>15/65</td><td>10</td><td></td><td></td><td>1.5</td><td>2.6</td><td>4.1</td><td>9.2</td><td>26.8</td></tr><tr><td>300</td><td>10/70</td><td>9</td><td>0.6</td><td>1.7</td><td>2.5</td><td>3.8</td><td>7.5</td><td>16.7</td><td>58.7</td></tr></table></body></html>  

<html><body><table><tr><td></td><td></td><td colspan="2">Calculations</td></tr><tr><td>Depth</td><td>Bottom Time</td><td>20 ft</td><td>10 ft</td></tr><tr><td>150</td><td>20</td><td>3</td><td>5</td></tr><tr><td></td><td></td><td></td><td></td></tr><tr><td>200</td><td>15</td><td>4</td><td>7</td></tr><tr><td></td><td></td><td></td><td></td></tr><tr><td>250</td><td>10</td><td>4</td><td>7</td></tr><tr><td></td><td></td><td></td><td></td></tr><tr><td>300</td><td>9</td><td>6</td><td>9</td></tr></table></body></html>  

Note that decompression on oxygen is independent of depth since the Equivalent Air Depth is -33 ft regardless of depth.  

# GRADIENT FACTOR  

The Gradient Factor (GF) does 2 things: $\mathrm{GF_{low}}$ sets the depth of the first decompression stop, and $\mathrm{{GF_{high}}}$ controls the NDL or, equivalently, the final decompression stop.  

<html><body><table><tr><td></td><td>GF=0.9</td><td>GF=0.8</td></tr><tr><td>Depth</td><td>Bottom Time</td><td>Bottom Time</td></tr><tr><td>40</td><td>110.4</td><td>87</td></tr><tr><td></td><td></td><td></td></tr><tr><td>60</td><td>42.7</td><td>34.2</td></tr><tr><td></td><td></td><td></td></tr><tr><td>80</td><td>22.3</td><td>17.4</td></tr><tr><td></td><td></td><td></td></tr><tr><td>100</td><td>13.8</td><td>11.5</td></tr></table></body></html>  

The table above represents my calculations for NDL bottom times for the 2 examples of GFhigh.  

For a 300 ft dive for a bottom time of $20\mathrm{min}$ using a mix of 10/70/20, my code indicated the depth of the first stop, DS, as indicated below.  

<html><body><table><tr><td>GFlow</td><td>DS</td></tr><tr><td>1.00</td><td>120</td></tr><tr><td>0.90</td><td>130</td></tr><tr><td>0.80</td><td>130</td></tr><tr><td>0.70</td><td>140</td></tr><tr><td>0.60</td><td>150</td></tr><tr><td>0.50</td><td>160</td></tr><tr><td>0.40</td><td>180</td></tr><tr><td>0.30</td><td>190</td></tr><tr><td>0.20</td><td>210</td></tr><tr><td>0.10</td><td>230</td></tr></table></body></html>  

The final example involves more than one gas.  A dive to 150 ft for a bottom time of 10 minutes using a $21/35/44\mathrm{mix}$ , and a constant $\mathrm{GF}=0.95$ .  We decompress at 10 ft using pure oxygen, which takes 2 minutes.  

# SUMMARY  

I believe that the preceding results produce a sufficient test of the validity of the Hollis Dive Computer and Dive Planner.  

# Closed Circuit Rebreather Governing Equations  

The CCR is characterized by a PPO2 (atm) set point, typically at 1.3 or 1.4 during the working portion of the dive and 1.6 during shallow decompression.  In addition, one must specify the composition of the diluent, $f_{\mathrm{O2}}/f_{\mathrm{He}}/f_{\mathrm{N2}}$ .  

The fundamental governing equation for the inert gases has not changed,  

$$
\frac{d\pi}{d t}=\frac{f(D+33)-\pi}{\tau}
$$  

However, $f$ is no longer a constant.  It is a function of depth governed by the following equations  

$$
f_{H e}=\left({\frac{f_{H e}}{f_{H e}+f_{N2}}}\right)_{D i l u e n t}\left(1-{\frac{P P O2}{\left(1+{\frac{D}{33}}\right)}}\right)
$$  

$$
f_{N2}=\left(\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i l u e n t}\left(1-\frac{P P O2}{\left(1+\frac{D}{33}\right)}\right)
$$  

Combining these new values of the effective fraction with the governing equation, we find with a little algebraic manipulation that  

$$
\frac{d\pi_{H e}}{d t}=\frac{\left(\displaystyle\frac{f_{H e}}{f_{H e}+f_{N2}}\right)_{D i l u e n t}\left[D-33\left(P P O2-1\right)\right]-\pi}{\tau}
$$  

$$
\frac{d\pi_{_{N2}}}{d t}=\frac{\left(\displaystyle\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}\left[D-33\left(P P O2-1\right)\right]-\pi}{\tau}
$$  

Descent from the surface at a rate of $\mathrm{D}_{\mathrm{R}}$  

$$
\frac{d\pi_{_{N2}}}{d t}=\frac{\displaystyle\left(\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}\left[D_{_R}t-33\left(P P O2-1\right)\right]-\pi_{_{N2}}}{\tau}
$$  

$$
\pi_{N2}=A+B t+C E x p\left(\frac{-t}{\tau}\right)
$$  

$$
\tau{\frac{d\pi_{_{N2}}}{d t}}=B\tau-C E x p\left({\frac{-t}{\tau}}\right)
$$  

$$
\tau\frac{d\pi_{N2}}{d t}+\pi_{N2}=A+B\left(t+\tau\right)=\left(\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i l u e n t}\left[D_{R}t-33\left(P P O2-1\right)\right]
$$  

$$
A+B\ \tau=-33\ (P P O2-1)\left({\frac{f_{N2}}{f_{H e}+f_{N2}}}\right)_{D i l u e n t}
$$  

$$
B=D_{R}\left({\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}}\right)_{D i l u e n t}
$$  

$$
A+C=\pi_{N2}(0)
$$  

$$
A=-\left({\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}}\right)_{D i l u e n t}\left[D_{_{R}}\ \tau+33\left(P P O2-1\right)\right]
$$  

$$
\begin{array}{c c c}{{\mathrm{}_{\tiny{N2}}=\pi_{\tiny{N2}}(0)\displaystyle E x p\left(\displaystyle\frac{-t}{\tau}\right)-\left(\displaystyle\frac{f_{\tiny{N2}}}{f_{\tiny{H e}}+f_{\tiny{N2}}}\right)_{D i l u e n t}\left[D_{\tiny{R}}\tau+33\left(P P O2-1\right)\right]\left[1-E x p\left(\displaystyle\frac{-t}{\tau}\right)\right]}}\\ {{+D_{R}t\left(\displaystyle\frac{f_{\tiny{N2}}}{f_{\tiny{H e}}+f_{\tiny{N2}}}\right)_{D i l u e n t}}}\end{array}
$$  

With a similar equation for helium, and at depth  

Descent from the surface at a rate of $\mathrm{D}_{\mathrm{R}}$ (cond)  

$$
\pi_{N2}(D)=\pi_{N2}(0)E x p\left({\frac{-t}{\tau}}\right)-\left({\frac{f_{N2}}{f_{H e}+f_{N2}}}\right)_{D i l u e n t}\left[D_{R}~\tau+33~(P P O2-1)\right]\left[1-E x p\left({\frac{-t}{\tau}}\right)\right]~,
$$  

$$
+D\left(\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}
$$  

$$
\pi_{\scriptscriptstyle{H e}}(D)=\pi_{\scriptscriptstyle{H e}}(0)E x p\left(\frac{-t}{\tau}\right)-\left(\frac{f_{\scriptscriptstyle{H e}}}{f_{\scriptscriptstyle{H e}}+f_{\scriptscriptstyle{N2}}}\right)_{D i l u e n t}\left[D_{\scriptscriptstyle{R}}\tau+33(P P O2-1)\right]\left[1-E x p\left(\frac{-t}{\tau}\right)\right],
$$  

$$
+D\left(\frac{f_{_{H e}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}
$$  

where $\mathrm{t}=\mathrm{D}/\mathrm{D}_{\mathrm{R}}$ At constant depth D  

$$
\frac{d\pi_{_{N2}}}{d t}=\frac{\left(\displaystyle\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}\left[D-33\left(P P O2-1\right)\right]-\pi_{_{N2}}}{\tau}
$$  

$$
\pi_{N2}=A+C~E x p\left(\frac{-t}{\tau}\right)
$$  

$$
\tau\frac{d\pi_{N2}}{d t}=-C E x p\left(\frac{-t}{\tau}\right)
$$  

$$
\tau{\frac{d\pi_{_{N2}}}{d t}}+\pi_{_{N2}}=A=\left({\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}}\right)_{D i l u e n t}\left[D-33(P P O2-1)\right]
$$  

$$
A+C=\pi_{N2}(0)
$$  

$$
\pi_{N2}(t)=\pi_{N2}(0)E x p\left(\frac{-t}{\tau}\right)+\left(\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i u e n t}\left[D-33\left(P P O2-1\right)\right]\left[1-E x p\left(\frac{-t}{\tau}\right)\right]
$$  

$$
\tau_{_{H e2}}(t)=\pi_{_{H e}}(0)E x p\left(\frac{-t}{\tau}\right)+\left(\frac{f_{_{H e}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i u e n t}\left[D-33\left(P P O2-1\right)\right]\left[1-E x p\left(\frac{-t}{\tau}\right)\right]\ .
$$  

Ascent from D at rate $\mathbf{A}_{\mathrm{R}}$  

$$
\frac{d\pi_{_{N2}}}{d t}=\frac{\left(\displaystyle\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}\left[D-A_{_{R}}t-33\left(P P O2-1\right)\right]-\pi_{_{N2}}}{\tau}
$$  

$$
\pi_{N2}=A+B t+C E x p\left(\frac{-t}{\tau}\right)
$$  

$$
\tau{\frac{d\pi_{_{N2}}}{d t}}=B\tau-C E x p\left({\frac{-t}{\tau}}\right)
$$  

$$
\tau\frac{d\pi_{_{N2}}}{d t}+\pi_{_{N2}}=A+B\left(t+\tau\right)=\left(\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{_{D i l u e n t}}\left[D-A_{_R}t-33\left(P P O2-1\right)\right]
$$  

$$
A+B\ \tau=\left[D-33(P P O2-1)\right]\left(\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i l u e n t}
$$  

$$
B=-A_{\scriptscriptstyle R}\left(\frac{f_{\scriptscriptstyle N2}}{f_{\scriptscriptstyle H e}+f_{\scriptscriptstyle N2}}\right)_{D i l u e n t}
$$  

$$
A+C=\pi_{N2}(0)
$$  

$$
A=\left(\frac{f_{_{N2}}}{f_{_{H e}}+f_{_{N2}}}\right)_{D i l u e n t}\left[D+A_{_R}\tau-33\left(P P O2-1\right)\right]
$$  

$$
\begin{array}{c c}{{\pi_{N2}=\pi_{N2}(0)~E x p\left(\displaystyle\frac{-t}{\tau}\right)+\left(\displaystyle\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i u e n t}\left[D+A_{R}\tau-33~(P P O2-1)\right]\left[1-E x p\left(\displaystyle\frac{-t}{\tau}\right)\right]}}\\ {{-A_{R}t\left(\displaystyle\frac{f_{N2}}{f_{H e}+f_{N2}}\right)_{D i u e n t}}}\end{array}
$$  

# HOLLIS PROPRIETARY INFORMATION  

Ascent from D at rate $\mathbf{A}_{\mathrm{R}}$ (cond)  

$$
\begin{array}{c}{{\cdot_{\scriptscriptstyle{N2}}(D S)=\pi_{\scriptscriptstyle{N2}}(0)~E x p\left(\displaystyle\frac{-t}{\tau}\right)+\left(\displaystyle\frac{f_{\scriptscriptstyle{N2}}}{f_{\scriptscriptstyle{H e}}+f_{\scriptscriptstyle{N2}}}\right)_{D i u e n t}\left[D+A_{\scriptscriptstyle{R}}\tau-33\left(P P O2-1\right)\right]\left[1-E x p\left(\displaystyle\frac{f_{\scriptscriptstyle{N2}}}{f_{\scriptscriptstyle{M e m}}}\right)\right]}}\\ {{-A_{\scriptscriptstyle{R}}t\left(\displaystyle\frac{f_{\scriptscriptstyle{N2}}}{f_{\scriptscriptstyle{H e}}+f_{\scriptscriptstyle{N2}}}\right)_{D i u e n t}}}\end{array}
$$  

$$
\begin{array}{c}{{\tiny{:\ }_{H e}(D S)=\pi_{H e}(0)\ E x p\left(\displaystyle{\frac{-t}{\tau}}\right)+\left(\displaystyle{\frac{f_{H e}}{f_{H e}+f_{N2}}}\right)_{D i l u e n t}\left[D+A_{R}\tau-33\left(P P O2-1\right)\right]\Bigg[1-E x p\left(\displaystyle{\frac{f_{H e}}{f_{H e}+f_{N2}}}\right)-1\Bigg]}}\\ {{-A_{R}t\left(\displaystyle{\frac{f_{H e}}{f_{H e}+f_{N2}}}\right)_{D i u e n t}}}\end{array}
$$  

# Tissue Loading Bar Graph (TLBG)  

In order to have a TLBG that is perfectly related to the tissue loading would require rewriting the entire algorithm document and its implementation.  Fortunately, I have found that a simple modification of the existing calculations will suffice.  The equations that I recommend are listed below where the term $\mathrm{TLBG}_{0}$ indicates the present calculations.  All are based on the projection of values to the decompression stop minus 10 ft.  If no decompression is required, project to the surface.  

$$
T L B G_{_0}=\left[\frac{\pi_{i N2}+\pi_{i H e}}{a_{i}+33/b_{i}}\right]_{M a x}
$$  

$$
T L B G_{_{N e w}}=\left[\frac{\pi_{i N2}-26.07+\pi_{i H e}}{a_{i}-26.07+33/b_{i}}\right]_{M a x}
$$  

Further, we use $\mathrm{TLBG}_{\mathrm{New}}$ when $\mathrm{TLBG}_{\mathrm{New}}<\mathrm{TLBG}_{0}$ , and $\mathrm{TLBG}_{0}$ when $\mathrm{TLBG}_{\mathrm{New}}>\mathrm{TLBG}_{0}$ .  See example below (air with $\mathrm{GF=0}.95$ ) where the transition takes place at $63\mathrm{min}$ .  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/8b23b143c268fd5cb39cc2dcff95be85b477e526ed697dc96ae9fb19c0a4a634.jpg)  

A second example of air with $\mathrm{GF=0.95}$ at a deeper depth of 100 ft is shown below (Table in Appendix A).  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/87ab34e9f308a9e252b1656329ae8210d6c003ecd66a50e3415694329e889d84.jpg)  

A third example of trimix 21/35 with $\mathrm{GF}{=}.95$ and a depth of 150 ft is shown below  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/d50d27c81b98c01e51f1b231ce9e0ac36558fd635271cfe7612eb2b84271f207.jpg)  

And a final fourth example of trimix 18/51 with $\mathrm{GF}{=}.95$ at a depth of 200 ft is shown below.  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/63e6325d7f5f9034cbe4a6e3a542a44668bf427594c58eba330f52500d18c912.jpg)  

Table values of all four examples can be found in Appendix. When the diver goes into decompression, the original calculations prevail, and I believe that you will find that the maximum value of TLBG for a 10 ft stop is 1.19, for a 20 ft stop is 1.38, etc.  

Note that the run times include the descent at a rate of $60\mathrm{fpm}$ , e.g., for 50 ft the descent takes 50 seconds, 100 ft 100 seconds, etc.  Finally, how the values are distributed on the display depends on the number of elements that are devoted to the TLBG.  For example, if we only have five elements, three for Green, one for Yellow, and one for Red  

# Gradient Factor Bar Graph  

We shall begin with the definition of the Gradient Factor (GF)  

$$
G F=\frac{\pi_{a l l o w a b l e}-\left(D+33\right)}{M-\left(D+33\right)}
$$  

$$
M=\frac{(D+33)}{b}+a
$$  

where $\pi$ is the sum of the N2 and He tensions, and a and b are the tension weighted values.  In order to calculate an appropriate value for a Gradient Factor Bar Graph (GFBG), we will try the definition shown below.  Note that when $\pi$ equals M, $\mathrm{GFBG}=1.0$ .  If we normalized the value by dividing by GF, we would have a graphic that differed little from the TLBG.  

$$
G F_{B a r G r a p h}=\frac{\pi-\left(D+33\right)}{M-\left(D+33\right)}
$$  

$$
M=\frac{(D+33)}{b}+a
$$  

Further, if we based the GFBG on the calculation at the depth for which the diver was at the present time, the GFBG would always be much smaller than GF because of the much larger values of M at depth.  We shall project the values of $\uppi$ and $\mathbf{M}$ to the stop depth less 10 ft.  For NoD situations this is reduced to the surface.  

The allowable values as specified by the user are $\mathrm{GF_{low}}$ , which is the allowable value at the deepest stop DS, and $\mathrm{{GF_{high}}}$ , which is the allowable value at the surface.  The allowable values between these depths are governed by the equation  

$$
G F(D)=G F_{l o w}+\Big(G F_{h i g h}-G F_{l o w}\Big)\Bigg(\frac{D S-D}{D S}\Bigg)
$$  

where $\mathrm{GF}(\mathrm{D})$ is the value at the depth D.  

A diver is not allowed to ascend past his present depth until $\pi$ is  

equal to or less than M for the next stop, i.e., $M=\frac{(D+23)}{b}+a$ . When the diver is at his last stop, $M={\frac{33}{b}}+a$ , with the exception of altitude where 33 becomes $\mathrm{\bfP_{\mathrm{init}}}$ .  

While a five segment bar graph is not useful, the following graph represents what could be used with a high resolution dot matrix.  

![](https://cdn-mineru.openxlab.org.cn/extract/6cf6a2d5-93f0-4142-9603-d2435ab2b1f3/66b80311aeabb9f1dfe29d98a195cb5d2de83e1e14e4b09b22abfd3b8c71795d.jpg)  

# Appendix A.  Gas Mix Recommendation  

# CNS Toxicity  

It is desirable to maximize the oxygen content that in turn minimizes any decompression obligation.  However, one must be careful to avoid the risk of CNS toxicity that could result in convulsions.  The controlling factor is the partial pressure of oxygen $(\mathrm{PPO}_{2})$ that depends upon the fraction of oxygen as well as the ambient pressure. Whereas a value of 1.6 atm is appropriate for the resting decompression portion of the dive, a smaller value is appropriate for the working portion, a value that is left for the user to select.  

$$
F O_{2}(\mathrm{max})={\frac{P P O_{2}(\mathrm{max})}{\left(1+{\frac{D}{P_{I n i t}}}\right)}}
$$  

Narcosis  

If we assume that oxygen is as narcotic as nitrogen, which is considered to become a factor for divers breathing air at depths in excess of about 100 ft, but we shall allow this air reference depth, $\mathrm{\DeltaD_{ref}}$ (narc), to be user selective.  When the maximum fraction of nitrogen is less than $1\mathrm{-FO}_{2}$ (the maximum available given a specified $\mathrm{FO}_{2}\mathrm{.}$ ), narcosis constrains $\mathrm{FN}_{2}$ by the following equation.  

$$
F N_{2}(\mathrm{max})=\frac{\left[1+\displaystyle\frac{D_{r e f}(n a r c)}{P_{I n i t}}\right]}{\left[1+\displaystyle\frac{D}{P_{I n i t}}\right]}-F O_{2}(\mathrm{max})
$$  

# Carbon Dioxide Retention  

There is the additional issue of carbon dioxide retention caused by the work of breathing, which increases the risk of CNS toxicity and is governed by the density of the breathing gas mixture.  Here again we shall allow the user to select the air reference depth, $\mathrm{D}_{\mathrm{ref}}$ (wob), and the controlling formula becomes  

$$
F N_{2}(\mathrm{max})=\frac{1.202\left[1+\frac{D_{r e f}\left(w o b\right)}{P_{m i t}}\right]-1.167~P P O_{2}(\mathrm{max})}{\left[1+\frac{D}{P_{m i t}}\right]}-0.167~\
$$  

For the example of PPO2 $(\mathrm{max})=1.3$ and using the default air reference value of 100 ft, the recommended values of maximum oxygen and minimum helium content (for sea level) are listed below.  Preliminary calculations indicate that the minimum value of helium may well be the optimum, but a final evaluation remains to be demonstrated when the code is completed.  

<html><body><table><tr><td>Depth (fsw)</td><td>FO2 (max)</td><td>FHe (min)</td><td>FN2(max)</td></tr><tr><td>0</td><td>1.00</td><td>0.00</td><td>0.00</td></tr><tr><td>10</td><td>1.00</td><td>0.00</td><td>0.00</td></tr><tr><td>20</td><td>0.81</td><td>0.00</td><td>0.19</td></tr><tr><td>30</td><td>0.68</td><td>0.00</td><td>0.32</td></tr><tr><td>40</td><td>0.59</td><td>0.00</td><td>0.41</td></tr><tr><td>50</td><td>0.52</td><td>0.00</td><td>0.48</td></tr><tr><td>60</td><td>0.46</td><td>0.00</td><td>0.54</td></tr><tr><td>70</td><td>0.42</td><td>0.00</td><td>0.58</td></tr><tr><td>80</td><td>0.38</td><td>0.00</td><td>0.62</td></tr><tr><td>90</td><td>0.35</td><td>0.00</td><td>0.65</td></tr><tr><td>100</td><td>0.32</td><td>0.02</td><td>0.66</td></tr><tr><td>110</td><td>0.30</td><td>0.10</td><td>0.60</td></tr><tr><td>120</td><td>0.28</td><td>0.17</td><td>0.55</td></tr><tr><td>130</td><td>0.26</td><td>0.23</td><td>0.51</td></tr><tr><td>140</td><td>0.25</td><td>0.28</td><td>0.47</td></tr><tr><td>150</td><td>0.23</td><td>0.33</td><td>0.43</td></tr><tr><td>160</td><td>0.22</td><td>0.38</td><td>0.40</td></tr><tr><td>170</td><td>0.21</td><td>0.41</td><td>0.37</td></tr><tr><td>180</td><td>0.20</td><td>0.45</td><td>0.35</td></tr><tr><td>190</td><td>0.19</td><td>0.48</td><td>0.33</td></tr><tr><td>200</td><td>0.18</td><td>0.51</td><td>0.30</td></tr><tr><td>210</td><td>0.18</td><td>0.54</td><td>0.29</td></tr><tr><td>220</td><td>0.17</td><td>0.56</td><td>0.27</td></tr><tr><td>230</td><td>0.16</td><td>0.59</td><td>0.25</td></tr><tr><td>240</td><td>0.16</td><td>0.61</td><td>0.24</td></tr><tr><td>250</td><td>0.15</td><td>0.63</td><td>0.22</td></tr><tr><td>260</td><td>0.15</td><td>0.65</td><td>0.21</td></tr><tr><td>270</td><td>0.14</td><td>0.66</td><td>0.20</td></tr><tr><td>280</td><td>0.14</td><td>0.68</td><td>0.18</td></tr><tr><td>290</td><td>0.13</td><td>0.69</td><td>0.17</td></tr><tr><td>300</td><td>0.13</td><td>0.71</td><td>0.16</td></tr></table></body></html>  

It is expected that the user will select a more standard trimix gas, examples of which are shown below (See 6 April 2011 memo entitled "Standard Trimix Gases" for references)  

<html><body><table><tr><td>Bottom Mix</td><td>Decompression Mix</td></tr><tr><td>Depth Mix</td><td>Depth Mix</td></tr><tr><td>10 to 100 33/0</td><td>O to 20 100/0</td></tr><tr><td>110 to 150 21/35</td><td>30 to 70 50/0</td></tr><tr><td>160 to 200 18/45</td><td>80 to 120 35/25</td></tr><tr><td>210 to 250 15/55</td><td>130 to 190 21/35</td></tr><tr><td>260 to 400 10/70</td><td></td></tr></table></body></html>  

Finally, using the user selection we shall calculate and display the equivalent air depth of narcosis and work of breathing.  

# Appendix B.  Derivation and Proof of Governing Equations  

# Derivation I  

Basic Transformation  

$$
\frac{d\pi}{d t}=\frac{f(D+33)-\pi}{\tau}
$$  

$$
\frac{d N}{d t}=\frac{D-N}{\tau}w h e r e N=\frac{\pi}{f}-33\
$$  

Descend at a rate $\mathrm{D}_{\mathrm{R}}$ from the surface  

$$
N=A+B t+C\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\tau{\frac{d N}{d t}}{=}B\tau-C\exp\left({\frac{-t_{d}}{\tau}}\right)
$$  

$$
A+B\tau=0B=D^{\prime}{}_{R}A+C=N_{0}
$$  

$$
A=-D_{\mathrm{~}R}^{\prime}\tau{}^{}=N_{\mathrm{~0~}}+D_{\mathrm{~}R}^{\prime}\tau
$$  

$$
N=-D_{\textit{R}}^{\prime}\tau+D_{\textit{R}}^{\prime}t_{d}+\left(N_{0}+D_{\textit{R}}^{\prime}\tau\right)\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\frac{\pi}{f}-33=-D{^{\prime}}_{R}~\tau+D{^{\prime}}_{R}~t_{d}+\left(\frac{\pi_{0}}{f}-33+D{^{\prime}}_{R}~\tau\right)\mathrm{exp}\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\pi=f(33-D{'}_{R}\ \tau+D)+\left[\pi_{0}-f(33-D{'}_{R}\ \tau)\right]\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
w h e r e\quad t_{d}=D/\operatorname*{D^{\prime}}_{R}
$$  

At a constant depth  

$$
\frac{d N}{d t}=\frac{D-N}{\tau}
$$  

$$
N=N_{0}\exp\left(\frac{-T}{\tau}\right)+D\left[1-\exp\left(\frac{-T}{\tau}\right)\right]
$$  

$$
N=D+(N_{\mathrm{0}}-D)\exp\left(\frac{-T}{\tau}\right)
$$  

$$
\frac{\pi}{f}-33=D+(\frac{\pi_{0}}{f}-33-D)\exp\left(\frac{-T}{\tau}\right)
$$  

$$
\pi=f(D+33)+\left[\pi_{0}-f(D+33)\right]\exp\left({\frac{-T}{\tau}}\right)
$$  

where $T$ is the time at depth $D$  

Ascend at a rate $\mathbf{A}_{\mathrm{{R}}}^{\prime}$  

$$
N=A+B t+C\exp\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
\tau{\frac{d N}{d t}}{=}B\tau-C\exp\left({\frac{-t_{a}}{\tau}}\right)
$$  

$$
A+B\tau=D B=-A^{\prime}{}_{R}A+C=N_{0}
$$  

$$
A=D+A_{\scriptscriptstyle R}^{\prime}\ \tau{}=N_{0}-D-A_{\scriptscriptstyle R}^{\prime}\ \tau
$$  

$$
N=D_{n e x t}+{\cal{A}^{\prime}}_{{\tiny_R}}\tau+\left(N_{\tiny_0}-D-{\cal{A}^{\prime}}_{\tiny R}\tau\right)\exp\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
\frac{\pi}{f}-33=D_{n e x t}+A_{_R}^{\prime}\tau+\left(\frac{\pi_{0}}{f}-33-D-A_{_R}^{\prime}\tau\right)\exp\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
\pi=f(33+D_{n e x t}+A_{\textit{R}}^{\prime}\tau)+\left[\pi_{0}-f(D+33+A_{\textit{R}}^{\prime}\tau)\right]\exp\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
w h e r e\mathrm{~\boldmath~\omega~}t_{a}=(D-D_{n e x t})/{A^{\prime}}_{R}
$$  

For ascent to surface $D_{n e x t}=0$  

# Derivation II  

Murat's universal equation, but using the appropriate values of ${\bf P}_{1}$ for different cases  

$$
\frac{d\pi}{d t}=\frac{P-\pi}{\tau}
$$  

For cons tant depth $P_{1}=D+33$  

$$
\pi(t)=f c\left(t-\tau\right)+f P_{1}+\left[\pi_{0}-f P_{1}+f c\tau\right]E x p{\left(\frac{-t}{\tau}\right)}
$$  

# Descent  

$$
\pi(t)=f D_{\phantom{}_{R}}^{\phantom{}^{\prime}}\left(t-\tau\right)+f33+\left[\pi_{0}-f33+f D_{\phantom{}_{R}}^{\prime}\tau\right]E x p\left({\frac{-t}{\tau}}\right)
$$  

$$
\pi(t)=f(D+33-D^{\prime}{}_{R}\tau)+\left[\pi_{0}-f\left(33-D^{\prime}{}_{R}\tau\right)\right]E x p\left({\frac{-t}{\tau}}\right)
$$  

Cons tant Depth  

$$
\pi(t)=f c\left({t-\tau}\right)+f P_{1}+\left[{\pi_{0}-f{\cal P}_{1}+f c\tau}\right]{\cal E}x p{\left(\frac{-t}{\tau}\right)}
$$  

$$
\pi(t)=f\left(D+33\right)+\left[\pi_{0}-f\left(D+33\right)\right]E x p{\left(\frac{-t}{\tau}\right)}
$$  

Ascent  

$$
\pi(t)=f c\left(t-\tau\right)+f P_{1}+\left[\pi_{0}-f P_{1}+f c\tau\right]E x p{\left(\frac{-t}{\tau}\right)}
$$  

$$
\pi(t)=f\left({D}_{n e x t}+33+{A^{\prime}}_{R}\ \tau\right)+\left[\pi_{0}-f\left(D+33+{A^{\prime}}_{R}\ \tau\right]E x p\middle({\frac{-t}{\tau}}\right)
$$  

# Proof by Differentiation  

Now differentiate the descent equation with the goal of reproducing the governing equation  

$$
\pi=f(33-D^{\prime}{}_{R}\ \tau+D)+\left[\pi_{0}-f(33-D^{\prime}{}_{R}\ \tau)\right]\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
w h e r e\quad t_{d}=D/\operatorname*{D}_{R}^{}
$$  

$$
\frac{d\pi}{d t}=f\frac{d D}{d t}-\frac{\left[\pi_{0}-f(33-D^{\prime}{}_{R}\tau)\right]}{\tau}\mathrm{exp}\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\tau\frac{d\pi}{d t}=f{\cal D^{\prime}}_{R}\tau-\left[\pi_{0}-f(33-{D^{\prime}}_{R}\tau)\right]\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\tau\frac{d\pi}{d t}+\pi=f D_{~R}^{\prime}\tau-\Big[\pi_{0}-f(33-D_{~R}^{\prime}\tau)\Big]\mathrm{exp}\left(\frac{-t_{d}}{\tau}\right)+f(33-D_{~R}^{\prime}\tau+D) 
$$  

$$
+\left[\pi_{0}-f(33-D^{\prime}{}_{R}\tau)\right]\exp\left(\frac{-t_{d}}{\tau}\right)=f(D+33)
$$  

$$
\frac{d\pi}{d t}=\frac{f(D+33)-\pi}{\tau}
$$  

Now differentiate the ascent equation with the goal of reproducing the governing equation  

$$
\pi=f(33+D_{n e x t}+A_{\textit{R}}^{\prime}\tau)+\left[\pi_{0}-f(D+33+A_{\textit{R}}^{\prime}\tau)\right]\exp\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
w h e r e t t_{a}=(D-D_{n e x t})/{A^{\prime}}_{R}
$$  

$$
\frac{d\pi}{d t}=f\frac{d D_{n e x t}}{d t}-\frac{\left[\pi_{0}-f(D+33+A_{_R}^{\prime}\tau)\right]}{\tau}\mathrm{exp}\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
\tau\frac{d\pi}{d t}=-f{A^{\prime}}_{R}\tau-\Big[\pi_{0}-f(D+33+A^{\prime}_{R}\tau)\Big]\mathrm{exp}\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
\begin{array}{c}{{\tau{\displaystyle\frac{d\pi}{d t}}+\pi=-f{\cal A^{\prime}}_{\kappa}\tau-\left[\pi_{0}-f(D+33+{\cal A^{\prime}}_{\kappa}\tau)\right]\mathrm{exp}\left({\displaystyle\frac{-t_{a}}{\tau}}\right)}}\\ {{+f(33+D_{n e x t}+{\cal A^{\prime}}_{\kappa}\tau)+\left[\pi_{0}-f(D+33+{\cal A^{\prime}}_{\kappa}\tau)\right]\mathrm{exp}\left({\displaystyle\frac{-t_{a}}{\tau}}\right)}}\end{array}
$$  

$$
\tau\frac{d\pi}{d t}+\pi=-f A_{\scriptscriptstyle R\scriptscriptstyle}^{\prime}\tau+f(33+D_{\scriptscriptstyle n e x t}+A_{\scriptscriptstyle R\scriptscriptstyle}^{\prime}\tau)=f(D_{\scriptscriptstyle n e x t}+33)
$$  

$$
\frac{d\pi}{d t}=\frac{f(D_{n e x t}+33)-\pi}{\tau}
$$  

# Proof by Limits  

Now try limits of $\mathrm{D}_{\mathrm{R}}^{\prime}<<1$ for the descent equation, i.e. very slow descent  

$$
\pi=f(33-D^{\prime}{}_{R}\ \tau+D)+\Big[\pi_{0}-f(33-D^{\prime}{}_{R}\tau)\Big]\mathrm{exp}\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\frac{t_{d}}{\tau}=\frac{D}{D_{\scriptscriptstyle{R}}^{\prime}\tau}\approx\infty
$$  

$$
\exp\left(\frac{-t_{d}}{\tau}\right)\approx0
$$  

$$
\pi=f\ (33+D)
$$  

Because of the very slow descent, memory of the initial condition is lost.  

Next try a very rapid descent $\mathrm{D}_{\mathrm{R}}^{\prime}{>}{>}1$  

$$
\pi=f(33-D^{\prime}{}_{R}\ \tau+D)+\left[\pi_{0}-f(33-D^{\prime}{}_{R}\ \tau)\right]\exp\left(\frac{-t_{d}}{\tau}\right)
$$  

$$
\frac{t_{d}}{\tau}=\frac{D}{D_{\textsl{R}}^{\prime}\tau}\approx0
$$  

$$
\exp\left(\frac{-t_{d}}{\tau}\right)\approx1-\frac{D}{D_{\mathrm{~}_{R}}^{\prime}}+\cdots
$$  

$$
\pi=f(33-D^{\prime}{}_{R}\ \tau+D)+\left[\pi_{0}-f(33-D^{\prime}{}_{R}\ \tau)\right]\left[1-{\frac{D}{D^{\prime}{}_{R}\ \tau}}+\cdots\right]
$$  

$$
\pi=f D+\pi_{0}-f D+\dots=\pi_{0}
$$  

The rapid descent allows no time for adjusting to local conditions and retains the initial value of $\pi$ unchanged.  

Now try limits of $\mathrm{A^{\prime}_{R}}{<}{<}1$ for the ascent equation, i.e. very slow ascent  

$$
\pi=f(33+D_{n e x t}+A_{\textit{R}}^{\prime}\tau)+\big[\pi_{0}-f(D+33+A_{\textit{R}}^{\prime}\tau)\big]\mathrm{exp}\left(\frac{-t_{a}}{\tau}\right)
$$  

$$
w h e r e\mathrm{~\textit~{~t~}~}_{a}=(D-D_{n e x t})/A_{\textit{R}}^{\prime}
$$  

$$
\frac{t_{a}}{\tau}=\frac{D}{A_{\scriptscriptstyle R}^{\prime}\tau}\approx\infty
$$  

$$
\exp\left(\frac{-t_{a}}{\tau}\right)\approx0
$$  

$$
\pi=f\left(33+D_{n e x t}\right)
$$  

And again for a very slow ascent the initial memory is lost and local conditions prevail.  

Now we try a very rapid ascent, i.e., $\mathbf{A}_{\mathrm{{R}}}^{\prime}>>1$  

$$
\pi=f(33+D_{n e x t}+A_{\textit{R}}^{\prime}\tau)+\left[\pi_{0}-f(D+33+A_{\textit{R}}^{\prime}\tau)\right]\exp{\left(\frac{-t_{a}}{\tau}\right)}
$$  

$$
\lambda h e r e\mathrm{~}t_{a}=(D-D_{n e x t})/{A^{\prime}}_{R}
$$  

$$
\frac{t_{a}}{\tau}=\frac{D}{A_{~R}^{\prime}\tau}
$$  

$$
\exp\left(\frac{-t_{a}}{\tau}\right)\approx1-\frac{D}{A_{\ R}^{*}\tau}+\cdots
$$  

$$
\pi=f(33+D_{n e x t}+A_{\scriptscriptstyle R}^{\prime}\ \tau)+\big[\pi_{0}-f(D+33+A_{\scriptscriptstyle R}^{\prime}\ \tau)\big]+f D+\cdots
$$  

$$
\pi=\pi_{0}+\cdots
$$  

And we see that for the rapid ascent there is no time for local affects, and the initial value of $\pi$ is unchanged.  

# HOLLIS PROPRIETARY INFORMATION  

# Proof by Numerical Integration  

<html><body><table><tr><td>Depth (ft)</td><td>300</td></tr><tr><td>Percent Nitrogen:</td><td>15%</td></tr><tr><td>Percent Helium:</td><td>71%</td></tr><tr><td>Descent Rate:</td><td>120</td></tr><tr><td>Ascent Rate:</td><td>60</td></tr></table></body></html>  

# Descent  

<html><body><table><tr><td></td><td colspan="3"> Coffee Numerical Calculation</td><td colspan="2">Lewis Analytical</td><td></td><td colspan="2">Diifferences</td><td></td></tr><tr><td>Compartment</td><td>Nitrogen</td><td>Helium</td><td>Total</td><td>Nitrogen</td><td>Helium</td><td>Total</td><td>Nitrogen</td><td>Helium</td><td>Total</td></tr><tr><td>1</td><td>25.46</td><td>93.41</td><td>118.87</td><td>25.45</td><td>93.39</td><td>118.84</td><td>0.0021</td><td>0.0270</td><td>0.0292</td></tr><tr><td>2</td><td>25.87</td><td>97.20</td><td>123.07</td><td>25.86</td><td>97.18</td><td>123.04</td><td>0.0016</td><td>0.0231</td><td>0.0247</td></tr><tr><td>3</td><td>26.26</td><td>94.49</td><td>120.75</td><td>26.26</td><td>94.48</td><td>120.74</td><td>0.0007</td><td>0.0144</td><td>0.0151</td></tr><tr><td>4</td><td>26.34</td><td>80.99</td><td>107.33</td><td>26.34</td><td>80.98</td><td>107.32</td><td>0.0003</td><td>0.0080</td><td>0.0083</td></tr><tr><td>5</td><td>26.31</td><td>65.27</td><td>91.59</td><td>26.31</td><td>65.27</td><td>91.58</td><td>0.0002</td><td>0.0044</td><td>0.0045</td></tr><tr><td>6</td><td>26.27</td><td>50.52</td><td>76.79</td><td>26.27</td><td>50.52</td><td>76.79</td><td>0.0001</td><td>0.0023</td><td>0.0024</td></tr><tr><td>7</td><td>26.22</td><td>38.46</td><td>64.69</td><td>26.22</td><td>38.46</td><td>64.69</td><td>0.0000</td><td>0.0013</td><td>0.0013</td></tr><tr><td>8</td><td>26.18</td><td>28.72</td><td>54.90</td><td>26.18</td><td>28.72</td><td>54.90</td><td>0.0000</td><td>0.0007</td><td>0.0007</td></tr><tr><td>9</td><td>26.15</td><td>21.07</td><td>47.22</td><td>26.15</td><td>21.07</td><td>47.22</td><td>0.0000</td><td>0.0003</td><td>0.0004</td></tr><tr><td>10</td><td>26.13</td><td>15.30</td><td>41.43</td><td>26.13</td><td>15.30</td><td>41.43</td><td>0.0000</td><td>0.0002</td><td>0.0002</td></tr><tr><td>11</td><td>26.12</td><td>11.63</td><td>37.75</td><td>26.12</td><td>11.63</td><td>37.75</td><td>0.0000</td><td>0.0001</td><td>0.0001</td></tr><tr><td>12</td><td>26.11</td><td>9.18</td><td>35.29</td><td>26.11</td><td>9.18</td><td>35.29</td><td>0.0000</td><td>0.0001</td><td>0.0001</td></tr><tr><td>13</td><td>26.10</td><td>7.26</td><td>33.36</td><td>26.10</td><td>7.26</td><td>33.36</td><td>0.0000</td><td>0.0000</td><td>0.0000</td></tr><tr><td>14</td><td>26.09</td><td>5.73</td><td>31.82</td><td>26.09</td><td>5.73</td><td>31.82</td><td>0.0000</td><td>0.0000</td><td>0.0000</td></tr><tr><td>15</td><td>26.09</td><td>4.49</td><td>30.58</td><td>26.09</td><td>4.49</td><td>30.58</td><td>0.0000</td><td>0.0000</td><td>0.0000</td></tr><tr><td>16</td><td>26.08</td><td>3.53</td><td>29.61</td><td>26.08</td><td>3.53</td><td>29.61</td><td>0.0000</td><td>0.0000</td><td>0.0000</td></tr><tr><td>17</td><td>26.08</td><td>2.78</td><td>28.86</td><td>26.08</td><td>2.78</td><td>28.86</td><td>0.0000</td><td>0.0000</td><td>0.0000</td></tr></table></body></html>  

# Ascent  

<html><body><table><tr><td></td><td colspan="3">Coffee Numerical Calculation</td><td colspan="2">Lewis Analytical</td><td></td><td colspan="2">Differences</td><td></td></tr><tr><td>Compartment</td><td>Nitrogen</td><td>Helium</td><td>Total</td><td>Nitrogen</td><td>Helium</td><td>Total</td><td>Nitrogen</td><td>Helium</td><td>Total</td></tr><tr><td>1</td><td>27.12</td><td>102.20</td><td>129.32</td><td>27.12</td><td>102.25</td><td>129.38</td><td>-0.0053</td><td>-0.0485</td><td>-0.0537</td></tr><tr><td>2</td><td>26.85</td><td>87.97</td><td>114.81</td><td>26.85</td><td>88.01</td><td>114.86</td><td>-0.0044</td><td>-0.0428</td><td>-0.0472</td></tr><tr><td>3</td><td>26.50</td><td>61.09</td><td>87.58</td><td>26.50</td><td>61.12</td><td>87.62</td><td>-0.0029</td><td>-0.0310</td><td>-0.0339</td></tr><tr><td>4</td><td>26.31</td><td>41.90</td><td>68.21</td><td>26.32</td><td>41.92</td><td>68.24</td><td>-0.0019</td><td>-0.0218</td><td>-0.0238</td></tr><tr><td>5</td><td>26.22</td><td>29.44</td><td>55.66</td><td>26.22</td><td>29.45</td><td>55.68</td><td>-0.0013</td><td>-0.0156</td><td>-0.0169</td></tr><tr><td>6</td><td>26.17</td><td>20.78</td><td>46.95</td><td>26.17</td><td>20.79</td><td>46.96</td><td>-0.0009</td><td>-0.0111</td><td>-0.0120</td></tr><tr><td>7</td><td>26.14</td><td>14.88</td><td>41.02</td><td>26.14</td><td>14.89</td><td>41.03</td><td>-0.0007</td><td>-0.0080</td><td>-0.0087</td></tr><tr><td>8</td><td>26.12</td><td>10.64</td><td>36.76</td><td>26.12</td><td>10.64</td><td>36.76</td><td>-0.0005</td><td>-0.0058</td><td>-0.0062</td></tr><tr><td>9</td><td>26.10</td><td>7.57</td><td>33.67</td><td>26.10</td><td>7.57</td><td>33.67</td><td>-0.0003</td><td>-0.0041</td><td>-0.0044</td></tr><tr><td>10</td><td>26.09</td><td>5.37</td><td>31.47</td><td>26.09</td><td>5.38</td><td>31.47</td><td>-0.0002</td><td>-0.0029</td><td>-0.0032</td></tr><tr><td>11</td><td>26.09</td><td>4.03</td><td>30.12</td><td>26.09</td><td>4.03</td><td>30.12</td><td>-0.0002</td><td>-0.0022</td><td>-0.0024</td></tr><tr><td>12</td><td>26.08</td><td>3.15</td><td>29.24</td><td>26.08</td><td>3.16</td><td>29.24</td><td>-0.0001</td><td>-0.0017</td><td>-0.0019</td></tr><tr><td>13</td><td>26.08</td><td>2.48</td><td>28.56</td><td>26.08</td><td>2.48</td><td>28.56</td><td>-0.0001</td><td>-0.0014</td><td>-0.0015</td></tr><tr><td>14</td><td>26.08</td><td>1.95</td><td>28.02</td><td>26.08</td><td>1.95</td><td>28.02</td><td>-0.0001</td><td>-0.0011</td><td>-0.0011</td></tr><tr><td>15</td><td>26.08</td><td>1.52</td><td>27.59</td><td>26.08</td><td>1.52</td><td>27.59</td><td>-0.0001</td><td>-0.0008</td><td>-0.0009</td></tr><tr><td>16</td><td>26.07</td><td>1.19</td><td>27.26</td><td>26.07</td><td>1.19</td><td>27.27</td><td>-0.0001</td><td>-0.0007</td><td>-0.0007</td></tr><tr><td>17</td><td>26.07</td><td>0.94</td><td>27.01</td><td>26.07</td><td>0.94</td><td>27.01</td><td>0.0000</td><td>-0.0005</td><td>-0.0006</td></tr></table></body></html>  

Appendix C.  Calculations  

<html><body><table><tr><td colspan="3">50 ft GF=.95 air</td><td colspan="3">100 ft GF=.95 air</td></tr><tr><td>Run time</td><td>TLBG0</td><td>TLBGNew</td><td>Run time</td><td>TLBG0</td><td>TLBGNev</td></tr><tr><td>0.00</td><td>0.631</td><td>0</td><td>0.00</td><td>0.631</td><td>0.000</td></tr><tr><td>0.17</td><td>0.631</td><td>0.07</td><td>0.17</td><td>0.631</td><td>0.074</td></tr><tr><td>0.33</td><td>0.631</td><td>0.09</td><td>0.33</td><td>0.631</td><td>0.085</td></tr><tr><td>0.50</td><td>0.631</td><td>0.1</td><td>0.50</td><td>0.631</td><td>0.103</td></tr><tr><td>0.67</td><td>0.631</td><td>0.13</td><td>0.67</td><td>0.631</td><td>0.126</td></tr><tr><td>0.83</td><td>0.631</td><td>0.15</td><td>0.83</td><td>0.631</td><td>0.154</td></tr><tr><td>1.00</td><td>0.631</td><td>0.164</td><td>1.00</td><td>0.631</td><td>0.186</td></tr><tr><td>2.00</td><td>0.631</td><td>0.217</td><td>1.17</td><td>0.631</td><td>0.220</td></tr><tr><td>3.00</td><td>0.631</td><td>0.261</td><td>1.33</td><td>0.634</td><td>0.258</td></tr><tr><td>4.00</td><td>0.631</td><td>0.299</td><td>1.50</td><td>0.635</td><td>0.297</td></tr><tr><td>5.00</td><td>0.631</td><td>0.332</td><td>1.67</td><td>0.636</td><td>0.338</td></tr><tr><td>6.00</td><td>0.631</td><td>0.362</td><td>2</td><td>0.637</td><td>0.365</td></tr><tr><td>7.00</td><td>0.631</td><td>0.389</td><td>3</td><td>0.639</td><td>0.440</td></tr><tr><td>8.00</td><td>0.631</td><td>0.411</td><td>4</td><td>0.641</td><td>0.507</td></tr><tr><td>9.00</td><td>0.631</td><td>0.431</td><td>5</td><td>0.67</td><td>0.565</td></tr><tr><td>10.00</td><td>0.631</td><td>0.45</td><td>6</td><td>0.714</td><td>0.616</td></tr><tr><td>20</td><td>0.756</td><td>0.61</td><td>7</td><td>0.754</td><td>0.663</td></tr><tr><td>30</td><td>0.805</td><td>0.72</td><td>8</td><td>0.791</td><td>0.713</td></tr><tr><td>40</td><td>0.864</td><td>0.8</td><td>9</td><td>0.824</td><td>0.760</td></tr><tr><td>50</td><td>0.892</td><td>0.88</td><td>10</td><td>0.855</td><td>0.802</td></tr><tr><td>60</td><td>0.957</td><td>0.95</td><td>12</td><td>0.909</td><td>0.877</td></tr><tr><td>63</td><td>0.971</td><td>0.97</td><td>14</td><td>0.955</td><td>0.939</td></tr><tr><td>70</td><td>0.997</td><td></td><td>16</td><td>0.994</td><td>0.992</td></tr><tr><td>80</td><td>1.03</td><td></td><td>17</td><td>1.01</td><td></td></tr><tr><td></td><td></td><td></td><td></td><td>200 ft GF=.95 18/51 Trimix</td><td></td></tr><tr><td>Run time</td><td>150 ft GF=.95 21/35 trimix TLBG0</td><td>TLBGNew</td><td>Run time</td><td>TLBG0</td><td>TLBGNew</td></tr><tr><td>0.00</td><td>0.631</td><td>0</td><td>0.00</td><td>0.631</td><td>0</td></tr><tr><td>0.33</td><td></td><td>0.122</td><td></td><td>0.631</td><td>0.14</td></tr><tr><td>0.67</td><td>0.632 0.633</td><td>0.184</td><td>0.33</td><td>0.633</td><td>0.214</td></tr><tr><td>1.00</td><td>0.634</td><td>0.254</td><td>0.67 1.00</td><td>0.635</td><td>0.293</td></tr><tr><td>1.33</td><td>0.637</td><td>0.329</td><td>1.33</td><td>0.638</td><td>0.381</td></tr><tr><td>1.67</td><td>0.64</td><td>0.415</td><td>1.67</td><td>0.642</td><td>0.476</td></tr><tr><td>2.00</td><td>0.643</td><td>0.505</td><td>2.00</td><td>0.684</td><td>0.571</td></tr><tr><td>2.33</td><td>0.704</td><td>0.597</td><td>2.33</td><td>0.763</td><td>0.673</td></tr><tr><td>2.5</td><td></td><td></td><td>2.67</td><td>0.845</td><td>0.785</td></tr><tr><td></td><td>0.741</td><td>0.644</td><td></td><td></td><td></td></tr><tr><td>3</td><td>0.778</td><td>0.69</td><td>3</td><td>0.928</td><td>0.898</td></tr><tr><td>4</td><td>0.845</td><td>0.782</td><td>3.1</td><td>0.992</td><td>0.985</td></tr><tr><td>5</td><td>0.905</td><td>0.866</td><td>3.2</td><td>1.0001</td><td></td></tr><tr><td>6</td><td>0.959</td><td>0.941</td><td></td><td></td><td></td></tr><tr><td>7</td><td>1.008</td><td></td><td></td><td></td><td></td></tr></table></body></html>  