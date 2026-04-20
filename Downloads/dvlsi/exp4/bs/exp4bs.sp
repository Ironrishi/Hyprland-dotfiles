transisent Responsse of cmos inverter, HRISHIKESH EXTC-17
VDD 1 0 5
Mp 3 2 1 1 PMOD W=20U L=1U
Mn 3 2 0 0 NMOD W=10U L=1U
CL 3 0 10pf
Vin 2 0 pulse(0 5 5ns 1ns 1ns 100ns 200ns)
.MODEL NMOD NMOS(VTO=1V, KP=100U)
.MODEL PMOD PMOS(VTO=-1V, KP=50U)
.tran 1ns 200ns
.control
run
plot V(2) v(3)
.endc
.end
