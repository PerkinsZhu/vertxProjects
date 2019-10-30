db.getCollection("ddd").find({ "_id": ObjectId("5d86eca5abbbcb5674f6e201") }, { "_id": false }).forEach(function (item) {
    for (i = 0; i < 50; i++) {
        array = []
        for (j = 0; j < 2000;j++) {
            array.push(item)
        }
        db.getCollection("ddd").insertMany(array)
    }
});

