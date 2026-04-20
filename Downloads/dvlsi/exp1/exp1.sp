
mosfet characteristics
Vdd 1 0 DC 5V
Vgg 3 0 DC 5V
Vd  1 2 DC 0V
M1  2 3 0 0 NMOD W=25U L=10U
.MODEL NMOD NMOS(KP=1000U, Vto=1V)
.DC Vdd 0 5 0.1
.control
run
plot i(vd)
.endc
.end

