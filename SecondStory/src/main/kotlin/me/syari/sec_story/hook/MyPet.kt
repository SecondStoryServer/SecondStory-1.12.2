package me.syari.sec_story.hook

import de.Keyle.MyPet.MyPetApi
import de.Keyle.MyPet.api.WorldGroup
import de.Keyle.MyPet.api.entity.MyPet
import de.Keyle.MyPet.api.entity.MyPetType
import de.Keyle.MyPet.api.event.MyPetCreateEvent
import de.Keyle.MyPet.api.event.MyPetSaveEvent
import de.Keyle.MyPet.api.player.MyPetPlayer
import de.Keyle.MyPet.api.repository.RepositoryCallback
import de.Keyle.MyPet.entity.InactiveMyPet
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.cmd
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object MyPet: Init(), Listener {
    private val hook = MyPetApi.getPlugin()
    private val repository = MyPetApi.getRepository()

    override fun init() {
        createCmd("pet",
            tab { element("info", "name", "pocket", "skill", "inv", "pick", "target", "beacon", "bye") }
        ){ sender, args ->
            if(sender is Player){
                val pet = sender.myPet ?: return@createCmd sender.send("&b[Pet] &cペットを持っていません")
                when(args.whenIndex(0)){
                    "info" -> {
                        sender.cmd("pinfo")
                    }
                    "name" -> {
                        val name = args.getOrNull(1) ?: return@createCmd sender.send("&b[Pet] &c新しく付ける名前を入力してください")
                        pet.petName = name
                        sender.send("&b[Pet] &fペットの名前を&a${name}&fにしました")
                    }
                    "pocket" -> {
                        when(pet.status){
                            MyPet.PetState.Dead -> {
                                sender.send("&b[Pet] &fペットは死んでしまっています")
                            }
                            MyPet.PetState.Despawned -> {
                                when(pet.createEntity()){
                                    MyPet.SpawnFlags.Success -> sender.send("&b[Pet] &fペットを呼び出しました")
                                    MyPet.SpawnFlags.NoSpace -> sender.send("&b[Pet] &cペットを呼び出すのに十分な場所がありません")
                                    MyPet.SpawnFlags.Flying -> sender.send("&b[Pet] &f飛んでいる状態で呼び出すことは出来ません")
                                    else -> sender.send("&b[Pet] &cペットを呼び出せませんでした")
                                }
                            }
                            MyPet.PetState.Here -> {
                                pet.removePet()
                                sender.send("&b[Pet] &fペットをしまいました")
                            }
                            else -> {
                                sender.send("&b[Pet] &cコマンドの実行に失敗しました")
                            }
                        }
                    }
                    "skill" -> {
                        if(pet.skilltree != null){
                            sender.cmd("pskill")
                        } else {
                            sender.cmd("pcst")
                        }
                    }
                    "inv" -> {
                        sender.cmd("petinventory")
                    }
                    "pick" -> {
                        sender.cmd("petpickup")
                    }
                    "target" -> {
                        sender.cmd("petbehavior")
                    }
                    "beacon" -> {
                        sender.cmd("petbeacon")
                    }
                    "bye" -> {
                        sender.cmd("petrelease")
                    }
                    else -> {
                        sender.send("""
                            &b[Pet] &fコマンド一覧
                            &7- &a/pet info &7ペットの情報を表示します
                            &7- &a/pet name &7ペットの名前を変更します
                            &7- &a/pet pocket &7ペットを出したりしまったりします
                            &7- &a/pet skill &7ペットのスキルを管理するコマンドです
                            &7- &a/pet inv &7ペットのインベントリを表示します
                            &7- &a/pet pick &7ペットがアイテムを拾うかどうかを選択します
                            &7- &a/pet target &7ペットの敵を選択します
                            &7- &a/pet beacon &7ペットのビーコンを管理するコマンドです
                            &7- &a/pet bye &7ペットを手放します
                        """.trimIndent())
                    }
                }
            }
        }
    }

    private val Player.myPet: MyPet?
        get() = hook.myPetManager.getMyPet(this)

    fun getPetType(name: String): MyPetType? = MyPetType.byEntityTypeName(name)

    private val Player.myPetPlayer: MyPetPlayer?
        get() = hook.playerManager.getMyPetPlayer(this)

    private val Player.myPetPlayerOrCreate: MyPetPlayer
        get() = myPetPlayer ?: hook.playerManager.registerMyPetPlayer(this)

    val Player.hasPet: Boolean
        get() = myPetPlayer?.hasMyPet() ?: false

    fun Player.getPet(type: MyPetType) {
        if(hasPet) return
        val data = myPetPlayerOrCreate
        val pet = InactiveMyPet(data)
        pet.petType = type
        pet.petName = "${name}のペット"
        val wg = WorldGroup.getGroupByWorld(world.name)
        pet.worldGroup = wg.name
        plugin.server.pluginManager.callEvent(MyPetCreateEvent(pet, MyPetCreateEvent.Source.Other))
        plugin.server.pluginManager.callEvent(MyPetSaveEvent(pet))
        repository.addMyPet(pet, object : RepositoryCallback<Boolean>() {
            override fun callback(added: Boolean) {
                if (added && !hasPet) {
                    pet.owner.setMyPetForWorldGroup(wg, pet.uuid)
                    repository.updateMyPetPlayer(pet.owner , null as? RepositoryCallback<Boolean>?)
                    val myPet = MyPetApi.getMyPetManager().activateMyPet(pet)
                    if (myPet.isPresent) {
                        myPet.get().createEntity()
                    }
                }
            }
        })
    }
}