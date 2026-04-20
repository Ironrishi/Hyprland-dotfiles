ΝAND GATE Hrishikesh - 17

vdd 1 0 dc 5v

vb 3 0 dc 5v

mp1 4 2 1 1 pmod w=20u l=1u
mp2 4 3 1 1 pmod w=20u l=1u
mn1 4 2 5 0 nmod w=5u l=1u
mn2 5 3 0 0 nmod w=5u l=1u
cl 4 Ο 10pf


va 2 0 pulse (0 5 5ns 1ns 1ns 100ns 200ns) 

.model pmod pmos(vto=-1V, kp=50u)
.model nmod nmos(vto=1V, kp=100u)
.tran 1ns 400ns
.control
run
plot v(2) v(4)
.endc
.end
