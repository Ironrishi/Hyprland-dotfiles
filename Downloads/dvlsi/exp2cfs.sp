MOSFET Scaling Hrishikesh - 17

VDS 1 0 DC 2.5V
VGS 3 0 DC 2.5V
VD 1 2 DC 0V
M 2 3 0 0 NMOD W=5U L=0.5U
.MODEL NMOD NMOS (KP=200U, VTO=0.5V)
.op
.control
run
print i(VD)
print i(VD)*V(1)
.endc
.end
