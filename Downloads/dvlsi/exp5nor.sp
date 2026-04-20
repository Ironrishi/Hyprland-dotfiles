ΕΧΡΤ 5 NOR GhTE AΜΕΥ 22
Vdd 1 0 DC 5V
Vb 3 0 DC 0V
MP1 5 2 1 1 PMOD W=20u L=1u
MP2 4 3 5 1 PMOD W=20u L=1u
MN1 4 2 0 0 NMOD W=5u L=1u
MN2 4 3 0 0 NMOD W=5u L=1u
CL 4 Ο 10pf
Va 2 0 PULSE (0 5 5ns 1ns 1ns 100ns 200ns) 
.MODEL PMOD PMOS(Vto=-1v, kp=50u)
.MODEL NMOD NMOS(Vto=1v, kp=100u)
.tran 1ns 400ns
.control
run
plot v(2) v(4)
.endc
.end
