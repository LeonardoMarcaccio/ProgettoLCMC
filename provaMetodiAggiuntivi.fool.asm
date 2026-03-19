push 0
push 15
push 3
sub
push 3
div
push function0
lfp
lfp
push -2
add
lw
lfp
stm
ltm
ltm
push -3
add
lw
js
push 1
beq label7
push 1
b label8
label7:
push 0
label8:
print
halt

function0:
cfp
lra
push 0
lfp
push 1
add
lw
bleq label3
push 0
b label4
label3:
push 1
label4:
push 1
beq label0
push 0
b label2
label0:
lfp
push 1
add
lw
push 10
bleq label5
push 0
b label6
label5:
push 1
label6:
push 1
beq label1
push 0
b label2
label1:
push 1
label2:
stm
sra
pop
pop
sfp
ltm
lra
js