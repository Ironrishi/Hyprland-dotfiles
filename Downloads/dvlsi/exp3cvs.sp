cmos inverter Neha - 18 s=1.9s

VDD 1 0 DC 5V 
VIN 3 0 DC 5V
VD 1 2 DC 0V
MP 4 3 2 2 PMOD W=10.52U L=0.5263U 
MN 4 3 0 0 NMOD W=5.263U L=0.5263U 
CL 4 0 2.77pF
.MODEL PMOD PMOS (KP=95U, VTO=1V)
.MODEL NMOD NMOS (KP=190U, VTO=1V)
.DC VIN 0 5 0.05
.control
run
plot V(4) V(3)
plot deriv(V(4)) 
plot i(VD)*V(1)
.endc
.end
