package com.perkins.bean

class User constructor(var name: String, var age: Int){
    override fun hashCode(): Int {
        return (name+age).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || !(other is User)){
            return false
        }
        return this.name.equals(other.name) && this.age.equals(other.age)
    }
}