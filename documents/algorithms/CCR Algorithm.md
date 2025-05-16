# I. TENSION IN TISSUE  

$\mathrm{Tn}2(\mathrm{t})$ the tension in nitrogen in the tissue. $\mathbf{k}$ the tissue resistance in ongasing / offgasing $\ensuremath{\mathrm{Pn}}2(\mathrm{t})$ the ambiant partial pressure of nitrogen (breathed mix)  

$$
\boldsymbol{k}\cdot\boldsymbol{T_{n2}}+\boldsymbol{T_{n2}}^{\prime}=\boldsymbol{k}\cdot\boldsymbol{P_{n2}}
$$  

solve DE interactively  

$$
k T_{n2}(x)+{\frac{\mathrm{d}}{\mathrm{d}x}}\ T_{n2}(x)=k P_{n2}
$$  

$$
T_{n2}(x)=P_{n2}+\mathrm{e}^{-k x}\left(T0-P_{n2}\right)
$$  

# II. CCR GENERAL EQUATIONS (TRIMIX)  

Following equations use depth now expressed in ft. Let - fo2: the composition of the breathed mix in O2 - fn2: the composition of the breathed mix in N2 - fhe: the composition of the breathed mix in He  

Such that: $f o2+f n2+f n e=1$  

$$
f o2+f n2+f n e=1
$$  

In shallow depth, when $\mathrm{SP}>=1.0$ , the PPO2 may be below SP. In such case, the breathed mix is pure oxygen. Therefore:  

$$
\begin{array}{c}{{P_{n2}:=0}}\\ {{}}\\ {{P_{h e}:=0}}\end{array}
$$  

$$
k\cdot T(x)+T=k\cdot P_{n2}
$$  

solve DE interactively  

$$
k T(x)+{\frac{\mathrm{d}}{\mathrm{d}x}}\ T(x)=0
$$  

$$
T(x)=T\theta\mathrm{e}^{-k x}
$$  

b. $\mathbf{P}\mathbf{P}\mathbf{O}2=\mathbf{S}\mathbf{P}$  

When PPO2 equals SP (deep enough, or SP low enough), a constant speed ascent or descent (or constant depth for $\scriptstyle\mathbf{v=}0$ ) is assumed.  

$D e p t h({t}):=D e p t h{0}+\nu\cdot{t}$  

$$
t{\rightarrow}D e p t h0+{\nu}t
$$  

$$
\displaystyle P a m b(t):=\frac{D e p t h(t)}{33}+1
$$  

$$
t\rightarrow\frac{1}{33}D e p t h(t)+1
$$  

$$
P_{o2}:=\mathrm{SP}
$$  

Due to the CCR mixing pure O2 and the diluent made with fn2, fhe and fo2:  

$$
P_{n2}(t):=\frac{P a m b(t)-\bar{S P}}{f_{n2}+f_{h e}}\cdot f_{n2}
$$  

$$
t\mathop{\rightarrow}\frac{\left(P a m b(t)-S P\right)f_{n2}}{f_{n2}+f_{h e}}
$$  

Similarly:  

$$
P_{h e}(t):=\dot{\frac{(P a m b(t)-S P)}{f_{n2}+f_{h e}}}\cdot f_{h e}
$$  

$$
t\rightarrow\frac{\left(P a m b(t)-S P\right)f_{h e}}{f_{n2}+f_{h e}}
$$  

The N2 tention in a tissue is expressed:  

$$
k T_{n2}(x)+{\frac{\mathrm{d}}{\mathrm{d}x}}\ T_{n2}(x)={\frac{k\left({\frac{1}{33}}\ \nu x+{\frac{1}{33}}\ D e p t h{}O+1-S P\right)f_{n2}}{f_{n2}+f_{h e}}}
$$  

solve DE interactively  

$$
\begin{array}{c}{{T_{n2}(x)=\left(\displaystyle\frac1{33}~\frac{\left(k\nu x+D e p t h0k-33~S P~k+33~k-\nu\right)f_{n2}\mathrm{e}^{k x}}{k\left(f_{n2}+f_{h e}\right)}+T0}}\\ {{-\displaystyle\frac1{33}~\frac{\left(D e p t h0k-33~S P~k+33~k-\nu\right)f_{n2}}{k\left(f_{n2}+f_{h e}\right)}\right)\mathrm{e}^{-k x}}}\end{array}
$$  

Similarly, the He tension in a tissue is expressed:  

$$
\begin{array}{c}{{k\cdot T_{h e}(x)+{T_{h e}}^{\prime}=k\cdot P_{h e}(x)}}\\ {{k T_{h e}(x)+\displaystyle{\frac{\mathrm{d}}{\mathrm{d}x}}T_{h e}(x)=\displaystyle{\frac{k\left(\displaystyle{\frac{1}{33}}\nu x+\displaystyle{\frac{1}{33}}D e p t h\theta+1-S P\right)f_{h e}}{f_{n2}+f_{h e}}}}}\end{array}
$$  

solve DE interactively  

$$
T_{h e}(x)=\left({\frac{1}{33}}\ {\frac{(k\nu x+D e p t h0k-33\ S P\ k+33\ k-\nu)f_{h e}\mathrm{e}^{k x}}{k\left(f_{n2}+f_{h e}\right)}}+T0\right.
$$  

$$
-\frac{1}{33}\frac{\left(D e p t h O k-33S P k+33k-\nu\right)f_{h e}}{k\left(f_{n2}+f_{h e}\right)}\Biggr)\mathrm{e}^{-k x}
$$  

Buhlmann's model defines the tissue time constants $\tau$ as the inverse of $\mathbf{k}$ -  

Note: the equations 14 and 16 have to be simplified and the units converted before implementing. The tension is expressed in bar, and the depth in feet.  

# III. Glossary  

$\boldsymbol{\uptau}$ : time constant of the studied tissue. $\mathbf{k}$ : resistance of the studied tissue in change of tension  

Pamb: ambient pressure, also equals the pressure of the breathed gas Pn2: partial pressure of nitrogen of the breathed gas   
Phe: partial pressure of helium of the breathed gas   
Po2: partial pressure of oxygen of the breathed gas   
Tn2: tension in nitrogen in the studied tissue   
The: tension in helium in the studied tissue   
T0: tension of the studied gas in the studied tissue at $\scriptstyle{\mathrm{t}}=0$   
Depth: depth of the diver in the water.   
Depth0: depth of the diver at $\scriptstyle{\mathrm{t}}=0$  