cmos inverter Hrishikesh - 17
VDD 1 0 DC 5V
VIN 3 0 DC 5V
VD 1 2 DC 0V
MP 4 3 2 2 PMOD W=20U L=1U
MN 4 3 0 0 NMOD W=10U L=1U
CL 4 0 10pF
.MODEL PMOD PMOS (VTO=-1V,Kp=50U)
.MODEL NMOD NMOS (VTO=1V,Kp=100U)
.DC VIN 0 5 0.05
.control
run
plot V(4) V(3) 
plot deriv(V(4)) 
plot i(VD)*V(1) 
.endc
.end

