transisent Responsse of cmos inverter, HRISHIKESH EXTC-17, s=1.9, c.v.s
VDD 1 0 2.63
Mp 3 2 1 1 PMOD W=10.52U L=0.52U
Mn 3 2 0 0 NMOD W=5.26U L=0.52U
CL 3 0 2.77pf
Vin 2 0 pulse(0 5 5ns 1ns 1ns 100ns 200ns)
.MODEL NMOD NMOS(VTO=0.27V, KP=190U)
.MODEL PMOD PMOS(VTO=-0.27V, KP=95U)
.tran 1ns 200ns
.control
run
plot V(2) v(3)
.endc
.end
