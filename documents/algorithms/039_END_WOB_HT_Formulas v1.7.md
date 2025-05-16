# 1. END (Equivalent Narcotic Depth) Calculations  

Note that during dive, the “current “ END shall be calculated. This is the depth where the narcotic effect of AIR is equal to the narcotic effect of currently breathed (actual) gas at current depth.  

Abviously, the END alarm is to be triggered according to the current END (if current END $>=$ END AL).  

However, in gas switch/set gas confirmation screens and depth limits screens the END displayed is the depth where the END of the gas is equal to the END ALARM set by the user. This can be called “Depth Limit for END Alarm”.  

# 1.1.Current END Calculations  

Imperial (N2 Only)  

$$
E N D\left(N_{2}\right)=\left\{\left[\left(D e p t h+P a m b\right)*\frac{F N_{2}}{0,79}\right]-P a m b\right\}
$$  

Imperial (O2 & N2) when Oxygen Narcotic is selected  

$$
E N D\ (O_{2}8N_{2})=\{[(D e p t h+P a m b)*(F O_{2}+F N_{2})]-P a m b\}
$$  

Pambis the surface level ambient pressure in FSW. At sea level $P a m b=33$ FSW  

$$
\mathrm{IF~END}<0\mathrm{then~END}=0
$$  

Example (O2 Non Narcotic); current depth $=200$ ft, Actual gas $\b=$ TX 18/45: Current $\mathbf{END}=76{,}13$ ft   (Note $\mathrm{FN}2=1\cdot0,18\cdot0,45)$  

Example (O2 Narcotic); current depth $=200$ ft, Actual gas $\b=$ TX 18/45: Current END $\mathbf{\tau}=$ 95,15 ft  

# 1.2.Depth Limit for END Alarm calculations  

Imperial (N2 Only)  

$$
E N D\ (N_{2})=(E N D\ A l a r m+P a m b)\times\frac{0,79}{F N2}-P a m b
$$  

Açıklamalı [sme1]: BU formül yanlış Pamb değil 33 olacak ama konvansiyon oalrak doğru kabul edilebilir DİKKAT ona gore manuel e yazılacak  

Imperial (O2 & N2)  

$$
E N D\left(N_{2}\right)=\left(E N D{\cal A}l a r m+P a m b\right)\times\frac{1}{\left(F O_{2}+F N_{2}\right)}-P a m b
$$  

IF $\mathbf{FN}2=0$ Then END limit $\b=$ Depth limit of dive computer (for instance 400 ft)  

# 2. HT (Hypoxic Threshold) Calculations  

The HT is the depth where the PO2 of the gas is equal to 0.21 ATA. This is calculated as follows:  

$$
H T\left(f t\right)={\left({\frac{6,93}{F O2}}\right)}-P a m b
$$  

Pamb is the surface level ambient pressure (33 FSW at sea level)  

$$
\scriptstyle{\mathrm{If~HT}}<0{\mathrm{~then~HT}}=0
$$  

The “current “ WOB is the depth where the WOB of AIR is equal to the WOB of breathed (actual) gas at current depth.  

The WOB alarm is to be triggered according to the current WOB (if current WOB $>=$ WOB AL).  

Depth Limit for WOB Alarm (DLWA) is the depth where the WOB of the gas is equal to the WOB ALARM set by the user.  

3. WOB and Depth Limit for WOB Alarm (DLWA) Calculations  

# 3.1.Current WOB Calculation  

$$
W O B=33\times\left\{\frac{[(F N2+0.167+1.167\times F O2)\times\left(\frac{P_{a m b}+D e p t h}{33}\right)]}{1.202}-\frac{P_{a m b}}{33}\right\}
$$  

Example: altitude: Sea Level $P a m b=33$ fsw), current depth $=200$ ft, Actual gas $\c=$ TX 18/45: Current $\mathsf{W O B}=111.81$ ft  

Example: altitude: 2000 ft $\cdot P a m b=30.73$ fsw), current depth $=200$ ft, Actual gas $\c=$ TX 18/45: Current $\mathbf{W0B}=112.67$ ft  

# 3.1.Depth Limit for WOB Alarm (DLWA) Calculation  

$$
D L W A=33*\left[{\frac{1.202*\left({\frac{P_{a m b}+W O B A l a r m}{33}}\right)}{(F N_{2}+0.167)+(1.167*F O_{2})}}-{\frac{P_{a m b}}{33}}\right]
$$  

Example: altitude: Sea Level $(P a m b=33$ fsw), WOB Alarm $=100$ ft, Actual gas $\c=$ TX 18/45: DLWA $=180.99\$ ft  

Example: altitude: 2000 ft $(P a m b=30.73$ fsw), WOB Alarm $=100$ ft, Actual gas $\c=$ TX 18/45: Current $\mathbf{W0B}=179.61$ ft  

# 4. Relation of FO2, FN2 and FHE  

For Trimix:  

$$
\begin{array}{r l}&{\mathtt{F O2}+\mathtt{F N2}+\mathtt{F H e}=1}\\ &{\mathtt{(F O2}+\mathtt{F N2}\mathtt{)}=\mathtt{F O2}+(1-\mathtt{(F O2}+\mathtt{F H e}))=1-\mathtt{F H e}}\end{array}
$$  