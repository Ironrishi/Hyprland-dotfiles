CMOS TRANSIENT RESPONSE Hrishikesh - 17

VDD 1 0 5
Mp 3 2 1 1 PMOD W=20U L=1U
Mn 3 2 0 0 NMOD W=10U L=1U
CL 4 0 10f
Vin 2 0 pulse(0 5 5ns 1ns 1ns 100ns 200ns)
.MODEL PMOD PMOS(VTO=-1V, Kp=50U)
.MODEL NMOD NMOS(VTO=1V, Kp=100U)
tran 1ns 200ns
.control
run
plot V(2) V(3)
.endc
.end
