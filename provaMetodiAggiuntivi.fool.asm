push 0
push 15
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
beq label2
push 1
b label3
label2:
push 0
label3:
print
halt

function0:
cfp
lra
push 10
lfp
push 1
add
lw
bleq label0
push 0
b label1
label0:
push 1
label1:
stm
sra
pop
pop
sfp
ltm
lra
js