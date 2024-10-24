# Display Editor Fabric
[![Java](https://img.shields.io/badge/Java-21-FF7700.svg?logo=java)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-186FCC.svg?logo=kotlin)]()
[![PaperMC](https://img.shields.io/badge/Latest-v1.2.1+mc1.21.3-222222.svg)]()

# command
/displayentity (dp)

## spawn
/dp spawn block [blockType][Properties] [x] [y] [z] [yaw] [pitch] [-center]

/dp spawn item with [itemType][components] [x] [y] [z] [yaw] [pitch]<br>
/dp spawn item from [block/entity] [x,y,z/Entity] [Slot] [x] [y] [z] [yaw] [pitch]

/dp spawn text [JSON_Text] [x] [y] [z] [yaw] [pitch] [backgroundColor]


## edit
/dp edit [Entity] block  [blockType][Properties]

/dp edit [Entity] item with [itemType][components]<br>
/dp edit [Entity] item from [block/entity] [x,y,z/Entity] [Slot]

/dp edit [Entity] text [JSON_Text] [backgroundColor]

/dp edit [Entity] transformation [values]

/dp edit [Entity] position [x] [y] [z]<br>
/dp edit [Entity] rotation [yaw] [pitch]<br>
/dp edit [Entity] teleport_duration [int]

/dp edit [Entity] billboard [fixed/center/vertical/horizontal]



## etc
/dp copydata [Entity]

## With WorldEdit

//pastedisplay(paste-d)<br>
※ Undo 불가 ※<br>
※ 디스플레이가 소환될곳에 있는 블럭을 지우고 소환되므로 기존 블럭을 남기려면 뒤에 -not-remove를 붙여주세요 ※