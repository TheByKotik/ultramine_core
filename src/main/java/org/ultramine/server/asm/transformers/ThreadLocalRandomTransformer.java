package org.ultramine.server.asm.transformers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.ultramine.server.UltraminePlugin;
import org.ultramine.server.asm.UMTBatchTransformer.IUMClassTransformer;
import org.ultramine.server.asm.UMTBatchTransformer.TransformResult;

import java.util.ListIterator;

/**
 * This transformer redirects field get to method invocation: <br />
 * from {@link net.minecraft.entity.Entity#rand} to {@link java.util.concurrent.ThreadLocalRandom#current()}
 */
public class ThreadLocalRandomTransformer implements IUMClassTransformer
{
	private static final Logger log = LogManager.getLogger();

	private static final String ENTITY_TYPE_OBF = "sa";
	private static final String ENTITY_TYPE_DEOBF = "net/minecraft/entity/Entity";
	private static final String FIELD_NAME_OBF = "Z";
	private static final String FIELD_DESC = "Ljava/util/Random;";
	private static final String TLR_TYPE = "java/util/concurrent/ThreadLocalRandom";
	private static final String METHOD_NAME = "current";
	private static final String METHOD_DESC = "()Ljava/util/concurrent/ThreadLocalRandom;";

	@Override
	public TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode)
	{
		boolean modified = false;
		for(MethodNode m : classNode.methods)
		{
			for(ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
			{
				AbstractInsnNode insnNode = it.next();
				if(insnNode.getOpcode() == Opcodes.GETFIELD)
				{
					FieldInsnNode fi = (FieldInsnNode)insnNode;
					String fieldNameObf = UltraminePlugin.isObfEnv ? "field_70146_Z" : "rand";
					if((FIELD_NAME_OBF.equals(fi.name) || fieldNameObf.equals(fi.name)) && FIELD_DESC.equals(fi.desc) && isEntityType(classNode, fi.owner))
					{
						log.trace("Method {}.{}{}: Replacing GETFIELD {}.random with INVOKESTATIC ThreadLocalRandom.current", name, m.name, m.desc, fi.owner);
						it.remove();
						// !it.hasPrevious() is impossible here
						if(it.previous().getOpcode() == Opcodes.ALOAD)
						{
							it.remove();
						}
						else
						{
							log.trace("\t\t-- Using POP insn");
							it.next();
							it.add(new InsnNode(Opcodes.POP));
						}
						it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TLR_TYPE, METHOD_NAME, METHOD_DESC, false));
						modified = true;
					}
				}
			}
		}

		return modified ? TransformResult.MODIFIED : TransformResult.NOT_MODIFIED;
	}

	private boolean isEntityType(ClassNode classNode, String fieldOwner)
	{
		if(ENTITY_TYPE_OBF.equals(fieldOwner) || ENTITY_TYPE_DEOBF.equals(fieldOwner))
			return true;
		if(!classNode.name.equals(fieldOwner))
			return false;

		try
		{
			Class<?> parent = this.getClass().getClassLoader().loadClass(classNode.superName.replace('/', '.'));
			if(parent.getSuperclass() == null)
				return false;
			while(parent.getSuperclass() != Object.class)
				parent = parent.getSuperclass();
			return parent.getName().equals("net.minecraft.entity.Entity");
		}
		catch(ClassNotFoundException e)
		{
			return false;
		}
	}
}
